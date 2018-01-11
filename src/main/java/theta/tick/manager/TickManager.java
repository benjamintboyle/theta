package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
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
import theta.tick.api.TickConsumer;
import theta.tick.api.TickMonitor;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.processor.TickProcessor;

public class TickManager implements TickMonitor, TickConsumer {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickSubscriber tickSubscriber;
  private PositionProvider positionProvider;
  private Executor executor;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final BlockingQueue<String> tickQueue = new LinkedBlockingQueue<String>();

  public TickManager(TickSubscriber tickSubscriber) {
    getStatus().changeState(ManagerState.STARTING);
    this.tickSubscriber = Objects.requireNonNull(tickSubscriber);
  }

  public Completable startTickProcessing() {

    logger.debug("Entered TickProcessing");

    return Completable.create(emitter -> {

      ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

      getStatus().changeState(ManagerState.RUNNING);

      while (getStatus().getState() == ManagerState.RUNNING) {

        // Blocks until tick available
        logger.info("Waiting for next tick across strike price.");
        final Tick tick = getLastTick();

        processTick(tick);
      }

      getStatus().changeState(ManagerState.SHUTDOWN);
    });
  }

  private Tick getLastTick() {

    Tick tick = null;

    String ticker = null;

    try {
      ticker = tickQueue.take();
    } catch (final InterruptedException e) {
      logger.error("Interupted while waiting for tick", e);
    }

    final Optional<TickHandler> optionalTickHandler = tickSubscriber.getHandler(ticker);

    if (optionalTickHandler.isPresent()) {
      final TickHandler tickHandler = optionalTickHandler.get();
      tick = new Tick(ticker, tickHandler.getLast(), TickType.LAST, tickHandler.getLastTime());
    } else {
      logger.warn("No Tick Handler for {}", ticker);
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

    final String ticker = priceLevel.getTicker();
    Optional<TickHandler> tickHandler = tickSubscriber.getHandler(ticker);

    if (!tickHandler.isPresent()) {
      logger.info("Adding Monitor for {}", ticker);

      tickHandler = Optional.ofNullable(tickSubscriber.subscribeTick(ticker, this));
    } else {
      logger.debug("Monitor exists for {}", ticker);
    }

    if (tickHandler.isPresent()) {
      tickHandler.get().addPriceLevel(priceLevel);
    } else {
      logger.error("Tick Handler not subscribed for {}", ticker);
    }
  }

  @Override
  public Integer deleteMonitor(PriceLevel priceLevel) {

    Integer priceLevelsMonitored = 0;

    final Optional<TickHandler> tickHandler = tickSubscriber.getHandler(priceLevel.getTicker());

    if (tickHandler.isPresent()) {
      priceLevelsMonitored = tickHandler.get().removePriceLevel(priceLevel);

      if (priceLevelsMonitored == 0) {
        logger.info("Deleting Tick Monitor for: {}", priceLevel.getTicker());
        tickSubscriber.unsubscribeTick(tickHandler.get());
        tickQueue.remove(priceLevel.getTicker());
      }
    } else {
      logger.warn("Tick Monitor for: {} does not exist", priceLevel.getTicker());
    }

    return priceLevelsMonitored;
  }

  private void processTick(Tick tick) {
    logger.info("Processing: {}", tick);

    if (tick.getTimestamp().isBefore(ZonedDateTime.now().minusSeconds(2))) {
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
