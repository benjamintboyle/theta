package theta.tick.manager;

import java.time.LocalDateTime;
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
	private final Logger logger = LoggerFactory.getLogger(TickManager.class);

	private TickSubscriber tickSubscriber;
	private PositionProvider positionProvider;
	private Executor executor;

	LocalDateTime lastChecked = LocalDateTime.MIN;

	private Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

	public TickManager(TickSubscriber tickSubscriber) {
		this.logger.info("Starting Tick Manager");
		this.tickSubscriber = tickSubscriber;
	}

	public TickManager(TickSubscriber tickSubscriber, PositionProvider positionProvider, Executor executor) {
		this(tickSubscriber);
		this.positionProvider = positionProvider;
		this.executor = executor;
	}

	@Override
	public void addMonitor(ThetaTrade theta) {
		this.logger.info("Adding Monitor for '{}'", theta.getBackingTicker());

		if (!this.tickHandlers.containsKey(theta.getBackingTicker())) {
			this.tickHandlers.put(theta.getBackingTicker(),
					this.tickSubscriber.subscribeEquity(theta.getBackingTicker()));
		}

		this.tickHandlers.get(theta.getBackingTicker()).addPriceLevel(theta);
	}

	public TickHandler deleteMonitor(String ticker) {
		logger.info("Deleting Tick Monitor for: {}", ticker);
		this.tickSubscriber.unsubscribeEquity(this.tickHandlers.get(ticker));
		return this.tickHandlers.remove(ticker);
	}

	private void reversePosition(ThetaTrade theta) {
		this.logger.info("Reversing position for '{}'}", theta);
		this.tickHandlers.get(theta.getBackingTicker()).removePriceLevel(theta);
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
	public void notifyTick(Tick tick) {
		logger.info("Received Tick from Handler: {}", tick);
		List<ThetaTrade> tradesToCheck = this.positionProvider.providePositions(tick.getTicker());

		logger.info("Received {} Positions from Position Provider", tradesToCheck.size());

		for (ThetaTrade theta : tradesToCheck) {
			logger.info("Checking Tick against position: {}", theta.toString());

			if (theta.getBackingTicker().equals(tick.getTicker())) {
				if (theta.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
					if (tick.getPrice() < theta.getStrikePrice()) {
						this.reversePosition(theta);
					}
				}

				if (theta.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
					if (tick.getPrice() > theta.getStrikePrice()) {
						this.reversePosition(theta);
					}
				}
			}
		}
	}
}