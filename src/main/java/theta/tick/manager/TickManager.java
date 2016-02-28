package theta.tick.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.ThetaTrade;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;
import theta.tick.domain.Tick;

public class TickManager implements Monitor, Runnable {
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
	public void addMonitor(ThetaTrade trade) {
		this.logger.info("Adding Monitor for '{}'", trade.getBackingTicker());

		if (!this.tickHandlers.containsKey(trade.getBackingTicker())) {
			this.tickHandlers.put(trade.getBackingTicker(),
					this.tickSubscriber.subscribeEquity(trade.getBackingTicker()));
		}
	}

	public TickHandler deleteMonitor(String ticker) {
		this.tickSubscriber.unsubscribeEquity(this.tickHandlers.get(ticker));
		return this.tickHandlers.remove(ticker);
	}

	@Override
	public void run() {
		// TODO: Validate that each loop is < 100ms, and number of
		// positions/monitors
		// which causes it to exceed 100ms
		// TODO: Implement way to cleanly exit loop / shutdown
		while (true) {
			// Convert all updated TickHandlers to Ticks
			List<Tick> updatedTicks = this.tickHandlers.values().parallelStream()
					.filter(handler -> handler.getLastTime().isAfter(this.lastChecked))
					.map(handler -> handler.getTick()).collect(Collectors.toList());
			// Update lastChecked time
			updatedTicks.parallelStream().map(tick -> tick.getTimestamp()).max(LocalDateTime::compareTo)
					.ifPresent(maxTime -> this.lastChecked = maxTime);
			// For updated Ticks get current positions
			List<ThetaTrade> tradesToCheck = this.positionProvider.providePositions(
					updatedTicks.parallelStream().map(handlers -> handlers.getTicker()).collect(Collectors.toSet()));
			// For each tick, check current position
			updatedTicks.parallelStream()
					.forEach(tick -> this.checkTick(
							tradesToCheck.parallelStream()
									.filter(trades -> trades.getBackingTicker().equals(tick.getTicker())).findAny(),
							tick));
		}
	}

	public void checkTick(Optional<ThetaTrade> optionalTheta, Tick tick) {
		if (!optionalTheta.isPresent()) {
			return;
		}

		ThetaTrade theta = optionalTheta.get();

		Double lastPrice = tick.getPrice();
		Integer quantity = theta.getEquity().getQuantity();
		Double strikePrice = theta.getStrikePrice();

		this.logger.info("Current tick for '{}': ${}", tick.getTicker(), lastPrice);

		if ((quantity > 0) && (lastPrice < strikePrice)) {
			this.reversePosition(theta);
		} else if ((quantity < 0) && (lastPrice > strikePrice)) {
			this.reversePosition(theta);
		}
	}

	private void reversePosition(ThetaTrade theta) {
		this.logger.info("Reversing position for '{}'}", theta);
		this.executor.reverseTrade(theta);
	}

	public void registerExecutor(Executor executor) {
		this.executor = executor;
	}

	public void registerPositionProvider(PositionProvider positionProvider) {
		this.positionProvider = positionProvider;
	}
}
