package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.ThetaUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.domain.StockUtil;
import theta.domain.ThetaTrade;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.PriceLevel;
import theta.tick.api.TickMonitor;
import theta.tick.api.TickObserver;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.processor.TickProcessor;

public class TickManager implements Callable<ManagerStatus>, TickMonitor, TickObserver {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickSubscriber tickSubscriber;
  private PositionProvider positionProvider;
  private Executor executor;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final BlockingQueue<String> tickQueue = new LinkedBlockingQueue<String>();
  private final Map<String, TickHandler> tickHandlers = new HashMap<String, TickHandler>();

  public TickManager(TickSubscriber tickSubscriber) {
    logger.info("Starting Tick Manager");
    this.tickSubscriber = Objects.requireNonNull(tickSubscriber);

    getStatus().changeState(ManagerState.STARTING);
  }

  @Override
  public ManagerStatus call() {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    getStatus().changeState(ManagerState.RUNNING);

    while (getStatus().getState() == ManagerState.RUNNING) {

      Optional<Tick> optionalTick = Optional.empty();

      try {
        // Blocks until tick available
        logger.info("Waiting for next tick across strike price.");
        final Tick tick = getLastTick();

        optionalTick = Optional.ofNullable(tick);

      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for tick", e);
      }

      // Process Tick
      optionalTick.ifPresent(tick -> processTick(tick));
    }

    getStatus().changeState(ManagerState.SHUTDOWN);

    return getStatus();
  }

  private Tick getLastTick() throws InterruptedException {
    Tick tick = null;

    final String ticker = tickQueue.take();

    final Optional<TickHandler> optionalTickHandler = Optional.ofNullable(tickHandlers.get(ticker));

    if (optionalTickHandler.isPresent()) {
      final TickHandler tickHandler = optionalTickHandler.get();
      tick = new Tick(ticker, tickHandler.getLast(), TickType.LAST, tickHandler.getLastTime());
    } else {
      tick = getLastTick();
    }

    logger.info("Received notification of tick across strike price: {}", tick);

    return tick;
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
  public void addMonitor(PriceLevel priceLevel) {

    if (!tickHandlers.containsKey(priceLevel.getTicker())) {
      logger.info("Adding Monitor for '{}'", priceLevel);
      final TickHandler tickHandler = tickSubscriber.subscribeTick(priceLevel.getTicker(), this);
      tickHandlers.put(priceLevel.getTicker(), tickHandler);
    } else {
      logger.debug("Monitor already exists for '{}'", priceLevel);
    }

    logger.info("Current Monitors: {}", tickHandlers.keySet().stream().sorted().collect(Collectors.toList()));

    tickHandlers.get(priceLevel.getTicker()).addPriceLevel(priceLevel);
  }

  @Override
  public Integer deleteMonitor(PriceLevel priceLevel) {

    Integer priceLevelsMonitored = 0;

    if (tickHandlers.containsKey(priceLevel.getTicker())) {
      final TickHandler tickHandler = tickHandlers.get(priceLevel.getTicker());

      priceLevelsMonitored = tickHandler.removePriceLevel(priceLevel);

      if (priceLevelsMonitored == 0) {
        logger.info("Deleting Tick Monitor for: {}", priceLevel);
        tickSubscriber.unsubscribeTick(tickHandler);
        tickHandlers.remove(priceLevel.getTicker());
        tickQueue.remove(priceLevel.getTicker());
      }
    } else {
      logger.warn("Tick Monitor for: {} does not exist", priceLevel);
    }

    return priceLevelsMonitored;
  }

  private void processTick(Tick tick) {
    logger.info("Processing Tick: {}", tick);

    if (tick.getTimestamp().isBefore(ZonedDateTime.now().minusSeconds(5))) {
      logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
    }

    final List<ThetaTrade> tradesToCheck = positionProvider.providePositions(tick.getTicker());

    logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

    final TickProcessor thetaTickProcessor = new TickProcessor(tick);

    final List<ThetaTrade> stocksToReverse =
        tradesToCheck.stream().map(thetaTickProcessor).flatMap(List::stream).collect(Collectors.toList());

    for (final ThetaTrade theta : stocksToReverse) {
      deleteMonitor(theta);
    }

    for (final Stock stock : StockUtil.consolidateStock(stocksToReverse)) {
      executor.reverseTrade(stock);
    }
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
  }

  public void registerExecutor(Executor executor) {
    logger.info("Registering Executor with Tick Manager");
    this.executor = Objects.requireNonNull(executor, "Executor must not be null.");
  }

  public void registerPositionProvider(PositionProvider positionProvider) {
    logger.info("Registering Position Provider with Tick Manager");
    this.positionProvider = Objects.requireNonNull(positionProvider, "Position Provider must not be null.");
  }

}
