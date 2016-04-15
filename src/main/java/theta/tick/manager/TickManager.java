package theta.tick.manager;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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
import theta.tick.domain.TickType;

public class TickManager implements Monitor, TickObserver, Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TickManager.class);

	private TickSubscriber tickSubscriber;
	private PositionProvider positionProvider;
	private Executor executor;

	private Boolean running = true;

	private BlockingQueue<String> tickQueue = new LinkedBlockingQueue<String>();
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

	public void shutdown() {
		this.running = Boolean.FALSE;
	}

	@Override
	public void run() {
		while (this.running) {
			try {
				// Blocks until tick available
				Tick tick = this.getNextTick();

				if (tick.getTimestamp().isAfter(ZonedDateTime.now().minus(15, ChronoUnit.SECONDS))) {
					this.processTicks(tick);
				}
			} catch (InterruptedException e) {
				logger.error("Interupted while waiting for tick", e);
			}
		}
	}

	@Override
	public void notifyTick(String ticker) {
		logger.info("Received Tick from Handler: {}", ticker);
		if (!this.tickQueue.contains(ticker)) {
			try {
				this.tickQueue.put(ticker);
			} catch (InterruptedException e) {
				logger.error("Interupted without adding Tick", e);
			}
		}
	}

	@Override
	public void addMonitor(ThetaTrade theta) {

		if (!this.tickHandlers.containsKey(theta.getTicker())) {
			logger.info("Adding Monitor for '{}'", theta);
			TickHandler tickHandler = this.tickSubscriber.subscribeEquity(theta.getTicker(), this);
			this.tickHandlers.put(theta.getTicker(), tickHandler);
		} else {
			logger.info("Monitor already exists for '{}'", theta);
		}

		logger.info("Current Monitors: {}", this.tickHandlers.keySet().stream().sorted().collect(Collectors.toList()));

		this.tickHandlers.get(theta.getTicker()).addPriceLevel(theta);
	}

	@Override
	public Integer deleteMonitor(ThetaTrade theta) {

		Integer priceLevelsMonitored = 0;

		if (this.tickHandlers.containsKey(theta.getTicker())) {
			TickHandler tickHandler = this.tickHandlers.get(theta.getTicker());

			priceLevelsMonitored = tickHandler.removePriceLevel(theta);

			if (priceLevelsMonitored == 0) {
				logger.info("Deleting Tick Monitor for: {}", theta);
				this.tickSubscriber.unsubscribeEquity(tickHandler);
				this.tickHandlers.remove(theta.getTicker());
			}
		} else {
			logger.warn("Tick Monitor for: {} does not exist", theta);
		}

		return priceLevelsMonitored;
	}

	private void reversePosition(ThetaTrade theta) {
		logger.info("Reversing position for '{}'}", theta);
		this.deleteMonitor(theta);
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

	private void processTicks(Tick tick) {
		logger.info("Processing Tick: {}", tick);

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

	private Tick getNextTick() throws InterruptedException {
		String ticker = this.tickQueue.take();

		TickHandler tickHandler = this.tickHandlers.get(ticker);

		return new Tick(ticker, tickHandler.getLast(), TickType.LAST, tickHandler.getLastTime());
	}
}