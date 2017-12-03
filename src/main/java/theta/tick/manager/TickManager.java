package theta.tick.manager;

import java.lang.invoke.MethodHandles;
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
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickSubscriber tickSubscriber;
  private PositionProvider positionProvider;
  private Executor executor;

  private Boolean running = true;

  private final BlockingQueue<String> tickQueue = new LinkedBlockingQueue<String>();
  private final Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

  public TickManager(TickSubscriber tickSubscriber) {
    logger.info("Starting Tick Manager");
    this.tickSubscriber = tickSubscriber;
  }

  public TickManager(TickSubscriber tickSubscriber, PositionProvider positionProvider,
      Executor executor) {
    this(tickSubscriber);
    this.positionProvider = positionProvider;
    this.executor = executor;
  }

  public void shutdown() {
    running = Boolean.FALSE;
  }

  @Override
  public void run() {
    while (running) {
      try {
        // Blocks until tick available
        final Tick tick = getNextTick();

        if (tick.getTimestamp().isAfter(ZonedDateTime.now().minus(15, ChronoUnit.SECONDS))) {
          processTicks(tick);
        }
      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for tick", e);
      }
    }
  }

  @Override
  public void notifyTick(String ticker) {
    logger.info("Received Tick from Handler: {}", ticker);
    if (!tickQueue.contains(ticker)) {
      try {
        tickQueue.put(ticker);
      } catch (final InterruptedException e) {
        logger.error("Interupted without adding Tick", e);
      }
    }
  }

  @Override
  public void addMonitor(ThetaTrade theta) {

    if (!tickHandlers.containsKey(theta.getTicker())) {
      logger.info("Adding Monitor for '{}'", theta);
      final TickHandler tickHandler = tickSubscriber.subscribeEquity(theta.getTicker(), this);
      tickHandlers.put(theta.getTicker(), tickHandler);
    } else {
      logger.info("Monitor already exists for '{}'", theta);
    }

    logger.info("Current Monitors: {}",
        tickHandlers.keySet().stream().sorted().collect(Collectors.toList()));

    tickHandlers.get(theta.getTicker()).addPriceLevel(theta);
  }

  @Override
  public Integer deleteMonitor(ThetaTrade theta) {

    Integer priceLevelsMonitored = 0;

    if (tickHandlers.containsKey(theta.getTicker())) {
      final TickHandler tickHandler = tickHandlers.get(theta.getTicker());

      priceLevelsMonitored = tickHandler.removePriceLevel(theta);

      if (priceLevelsMonitored == 0) {
        logger.info("Deleting Tick Monitor for: {}", theta);
        tickSubscriber.unsubscribeEquity(tickHandler);
        tickHandlers.remove(theta.getTicker());
      }
    } else {
      logger.warn("Tick Monitor for: {} does not exist", theta);
    }

    return priceLevelsMonitored;
  }

  private void reversePosition(ThetaTrade theta) {
    logger.info("Reversing position for '{}'}", theta);
    deleteMonitor(theta);
    executor.reverseTrade(theta);
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

    final List<ThetaTrade> tradesToCheck = positionProvider.providePositions(tick.getTicker());

    logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(),
        tradesToCheck);

    for (final ThetaTrade theta : tradesToCheck) {
      logger.info("Checking Tick against position: {}", theta.toString());

      if (theta.getTicker().equals(tick.getTicker())) {
        if (theta.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
          if (tick.getPrice() < theta.getStrikePrice()) {
            reversePosition(theta);
          } else {
            logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}",
                PriceLevelDirection.FALLS_BELOW, tick, theta);
          }
        } else if (theta.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
          if (tick.getPrice() > theta.getStrikePrice()) {
            reversePosition(theta);
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
    final String ticker = tickQueue.take();

    final TickHandler tickHandler = tickHandlers.get(ticker);

    return new Tick(ticker, tickHandler.getLast(), TickType.LAST, tickHandler.getLastTime());
  }
}
