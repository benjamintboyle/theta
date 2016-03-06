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
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private TickSubscriber tickSubscriber;
	private PositionProvider positionProvider;
	private Executor executor;

	LocalDateTime lastChecked = LocalDateTime.MIN;

	private Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

	public TickManager(TickSubscriber tickSubscriber) {
		this.logger.info("Starting subsystem: 'Monitor'");
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
		this.tickSubscriber.unsubscribeEquity(this.tickHandlers.get(ticker));
		return this.tickHandlers.remove(ticker);
	}

	private void reversePosition(ThetaTrade theta) {
		this.logger.info("Reversing position for '{}'}", theta);
		this.tickHandlers.get(theta.getBackingTicker()).removePriceLevel(theta);
		this.executor.reverseTrade(theta);
	}

	public void registerExecutor(Executor executor) {
		this.executor = executor;
	}

	public void registerPositionProvider(PositionProvider positionProvider) {
		this.positionProvider = positionProvider;
	}

	@Override
	public void notifyTick(Tick tick) {
		List<ThetaTrade> tradesToCheck = this.positionProvider.providePositions(tick.getTicker());

		for (ThetaTrade theta : tradesToCheck) {
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