package theta.tick.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.ThetaTrade;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;
import theta.tick.api.PriceLevelDirection;
import theta.tick.api.TickObserver;
import theta.tick.domain.Tick;

public class TickManager implements Monitor, TickObserver {
	private static final Logger logger = LoggerFactory.getLogger(TickManager.class);

	private TickSubscriber tickSubscriber;
	private PositionProvider positionProvider;
	private Executor executor;

	private Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

	public TickManager(TickSubscriber tickSubscriber) {
		logger.info("Starting Tick Manager");
		this.tickSubscriber = tickSubscriber;
	}

	public TickManager(TickSubscriber tickSubscriber, PositionProvider positionProvider, Executor executor) {
		this(tickSubscriber);
		this.positionProvider = positionProvider;
		this.executor = executor;
	}

	@Override
	public void addMonitor(ThetaTrade theta) {
		logger.info("Adding Monitor for '{}'", theta.getTicker());

		if (!this.tickHandlers.containsKey(theta.getTicker())) {
			this.tickHandlers.put(theta.getTicker(), this.tickSubscriber.subscribeEquity(theta.getTicker(), this));
		}

		this.tickHandlers.get(theta.getTicker()).addPriceLevel(theta);
	}

	public TickHandler deleteMonitor(String ticker) {
		logger.info("Deleting Tick Monitor for: {}", ticker);
		this.tickSubscriber.unsubscribeEquity(this.tickHandlers.get(ticker));
		return this.tickHandlers.remove(ticker);
	}

	private void reversePosition(ThetaTrade theta) {
		logger.info("Reversing position for '{}'}", theta);
		this.tickHandlers.get(theta.getTicker()).removePriceLevel(theta);
		this.executor.reverseTrade(theta);
	}

	public void registerExecutor(Executor executor) {
		logger.info("Registering Executor with Tick Manager");
		this.executor = executor;
	}

	public void registerPositionProvider(PositionProvider positionProvider) {
		logger.info("Registering Position Provider with Tick Manager");
		this.positionProvider = positionProvider;
	}

	@Override
	public synchronized void notifyTick(Tick tick) {
		logger.info("Received Tick from Handler: {}", tick);
		List<ThetaTrade> tradesToCheck = this.positionProvider.providePositions(tick.getTicker());

		logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

		for (ThetaTrade theta : tradesToCheck) {
			logger.info("Checking Tick against position: {}", theta.toString());

			if (theta.getTicker().equals(tick.getTicker())) {
				if (theta.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
					if (tick.getPrice() < theta.getStrikePrice()) {
						this.reversePosition(theta);
					} else {
						logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}",
								PriceLevelDirection.FALLS_BELOW, tick, theta);
					}
				} else if (theta.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
					if (tick.getPrice() > theta.getStrikePrice()) {
						this.reversePosition(theta);
					} else {
						logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}",
								PriceLevelDirection.RISES_ABOVE, tick, theta);
					}
				} else {
					logger.error("Invalid Price Level: {}", theta.tradeIf());
				}
			}
		}
	}
}