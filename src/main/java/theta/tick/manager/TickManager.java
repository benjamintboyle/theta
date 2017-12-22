package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.ManagerState;
import theta.ThetaUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.PriceLevelDirection;
import theta.tick.api.TickMonitor;
import theta.tick.api.TickObserver;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;

public class TickManager implements Callable<ManagerState>, TickMonitor, TickObserver {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickSubscriber tickSubscriber;
  private PositionProvider positionProvider;
  private Executor executor;

  private ManagerState managerState = ManagerState.SHUTDOWN;

  private final BlockingQueue<String> tickQueue = new LinkedBlockingQueue<String>();
  private final Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

  public TickManager(TickSubscriber tickSubscriber) {
    logger.info("Starting Tick Manager");
    this.tickSubscriber = tickSubscriber;

    changeState(ManagerState.STARTING);
  }

  @Override
  public ManagerState call() {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    changeState(ManagerState.RUNNING);

    while (status() == ManagerState.RUNNING) {
      try {
        // Blocks until tick available
        logger.info("Waiting to be notified about tick across strike price.");
        final Tick tick = getNextTick();
        logger.info("Received notification of tick across strike price: {}", tick);

        processTick(tick);

      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for tick", e);
      }
    }

    changeState(ManagerState.SHUTDOWN);

    return status();
  }

  @Override
  public void acceptTick(String ticker) {
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
      logger.debug("Monitor already exists for '{}'", theta);
    }

    logger.info("Current Monitors: {}", tickHandlers.keySet().stream().sorted().collect(Collectors.toList()));

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

  private void changeState(ManagerState newState) {
    final ManagerState oldState = status();

    managerState = newState;

    logger.info("TickManager state transitioned from {} to {}", oldState, status());
  }

  public ManagerState status() {
    return managerState;
  }

  public void shutdown() {
    logger.info("Shutdown called");
    changeState(ManagerState.STOPPING);
  }

  private void reversePositions(List<ThetaTrade> thetas) {
    logger.info("Reversing position for '{}'}", thetas);

    // Remove monitors, and consolidate stock
    final Map<UUID, Stock> stocksToReverse = new HashMap<>();
    for (final ThetaTrade theta : thetas) {
      if (stocksToReverse.containsKey(theta.getStock().getId())) {
        final Optional<Stock> combinedStock = Stock.of(stocksToReverse.get(theta.getStock().getId()), theta.getStock());

        if (combinedStock.isPresent()) {
          stocksToReverse.put(theta.getStock().getId(), combinedStock.get());
        } else {
          logger.error("Stock with same Id cannot be combined: {} {}", theta.getStock(),
              stocksToReverse.get(theta.getId()));
        }
      } else {
        stocksToReverse.put(theta.getStock().getId(), theta.getStock());
      }

      deleteMonitor(theta);
    }

    for (final Stock stock : stocksToReverse.values()) {
      executor.reverseTrade(stock);
    }
  }

  public void registerExecutor(Executor executor) {
    logger.info("Registering Executor with Tick Manager");
    this.executor = executor;
  }

  public void registerPositionProvider(PositionProvider positionProvider) {
    logger.info("Registering Position Provider with Tick Manager");
    this.positionProvider = positionProvider;
  }

  private void processTick(Tick tick) {
    logger.info("Processing Tick: {}", tick);

    if (tick.getTimestamp().isBefore(ZonedDateTime.now().minusSeconds(5))) {
      logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
    }

    final List<ThetaTrade> tradesToCheck = positionProvider.providePositions(tick.getTicker());

    logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

    final List<ThetaTrade> tradesToReverse = new ArrayList<>();

    for (final ThetaTrade theta : tradesToCheck) {
      logger.info("Checking Tick against position: {}", theta.toString());

      if (theta.getTicker().equals(tick.getTicker())) {
        if (theta.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
          if (tick.getPrice() < theta.getStrikePrice()) {
            tradesToReverse.add(theta);
          } else {
            logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}", PriceLevelDirection.FALLS_BELOW, tick,
                theta);
          }
        } else if (theta.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
          if (tick.getPrice() > theta.getStrikePrice()) {
            tradesToReverse.add(theta);
          } else {
            logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}", PriceLevelDirection.RISES_ABOVE, tick,
                theta);
          }
        } else {
          logger.error("Invalid Price Level: {}", theta.tradeIf());
        }
      }
    }

    reversePositions(tradesToReverse);
  }

  private Tick getNextTick() throws InterruptedException {
    Tick tick = null;

    final String ticker = tickQueue.take();

    final Optional<TickHandler> optionalTickHandler = Optional.ofNullable(tickHandlers.get(ticker));

    if (optionalTickHandler.isPresent()) {
      final TickHandler tickHandler = optionalTickHandler.get();
      tick = new Tick(ticker, tickHandler.getLast(), TickType.LAST, tickHandler.getLastTime());
    } else {
      tick = getNextTick();
    }

    return tick;
  }
}
