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
import theta.api.TickSubscriber;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.domain.StockUtil;
import theta.domain.Theta;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.PriceLevel;
import theta.tick.api.TickConsumer;
import theta.tick.api.TickMonitor;
import theta.tick.domain.Tick;
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

        String ticker = null;

        try {
          // Blocks until tick available
          logger.info("Waiting for next tick across strike price.");
          ticker = tickQueue.take();
        } catch (final InterruptedException exception) {
          logger.error("Interupted while waiting for tick", exception);
          emitter.onError(exception);
        }

        final Optional<Tick> tick = tickSubscriber.getLastTick(ticker);

        if (tick.isPresent()) {
          logger.info("Received notification of tick across strike price: {}", tick.get());

          processTick(tick.get());
        } else {
          logger.warn("No Tick available for {}", ticker);
        }
      }

      getStatus().changeState(ManagerState.SHUTDOWN);
    });
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

    tickSubscriber.addPriceLevelMonitor(priceLevel, this);
  }

  @Override
  public Integer deleteMonitor(PriceLevel priceLevel) {

    return tickSubscriber.removePriceLevelMonitor(priceLevel);
  }

  private void processTick(Tick tick) {

    logger.info("Processing: {}", tick);

    if (tick.getTimestamp().isBefore(ZonedDateTime.now().minusSeconds(2))) {
      logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
    }

    final List<Theta> tradesToCheck = positionProvider.providePositions(tick.getTicker());

    if (tradesToCheck.size() == 0) {
      logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

      final TickProcessor thetaTickProcessor = new TickProcessor(tick);

      final List<Theta> stocksToReverse =
          tradesToCheck.stream().map(thetaTickProcessor).flatMap(List::stream).collect(Collectors.toList());

      for (final Theta theta : stocksToReverse) {
        deleteMonitor(theta);
      }

      for (final Stock stock : StockUtil.consolidateStock(stocksToReverse)) {
        executor.reverseTrade(stock);
      }
    } else {
      logger.warn("Unsubscribing Tick Monitor for {}. Received 0 Positions from Position Provider for Tick: {}",
          tick.getTicker(), tick);

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
