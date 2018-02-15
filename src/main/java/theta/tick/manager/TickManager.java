package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.api.TickSubscriber;
import theta.domain.DefaultPriceLevel;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.domain.StockUtil;
import theta.domain.Theta;
import theta.domain.api.PriceLevel;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Tick;
import theta.tick.api.TickMonitor;
import theta.tick.api.TickProcessor;

public class TickManager implements TickMonitor {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickSubscriber tickSubscriber;
  private final TickProcessor tickProcessor;

  private PositionProvider positionProvider;
  private Executor executor;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final CompositeDisposable tickManagerDisposables = new CompositeDisposable();

  public TickManager(TickSubscriber tickSubscriber, TickProcessor tickProcessor) {
    getStatus().changeState(ManagerState.STARTING);
    this.tickSubscriber = Objects.requireNonNull(tickSubscriber, "Tick Subscriber cannot be null.");
    this.tickProcessor = Objects.requireNonNull(tickProcessor, "Tick Processor cannot be null.");
  }

  public Completable startTickProcessing() {

    logger.debug("Starting Tick Processing");

    return Completable.create(emitter -> {

      Disposable tickSubscriberDisposable = tickSubscriber.getTicksAcrossStrikePrices().subscribe(

          tick -> {
            processTick(tick);
          },

          exception -> {
            logger.error("Error in Tick Manager", exception);
          },

          () -> {
            getStatus().changeState(ManagerState.SHUTDOWN);
          },

          subscription -> {
            getStatus().changeState(ManagerState.RUNNING);
            subscription.request(Long.MAX_VALUE);
          });

      tickManagerDisposables.add(tickSubscriberDisposable);
    });
  }

  @Override
  public void addMonitor(PriceLevel priceLevel) {

    tickSubscriber.addPriceLevelMonitor(priceLevel, tickProcessor);
  }

  @Override
  public Integer deleteMonitor(PriceLevel priceLevel) {

    return tickSubscriber.removePriceLevelMonitor(priceLevel);
  }

  private void processTick(Tick tick) {

    logger.debug("Processing: {}", tick);

    if (tick.getTimestamp().isBefore(ZonedDateTime.now().minusSeconds(2))) {
      logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
    }

    final List<Theta> tradesToCheck = positionProvider.providePositions(tick.getTicker());

    if (tradesToCheck.size() > 0) {

      logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

      final List<Theta> thetasToReverse =
          tradesToCheck.stream().filter(theta -> tickProcessor.process(tick, DefaultPriceLevel.of(theta))).collect(
              Collectors.toList());

      // FIXME: This doesn't correctly calculate limit price
      for (final Stock stock : StockUtil.consolidateStock(thetasToReverse)) {

        Disposable disposableTrade = executor
            .reverseTrade(stock, tickProcessor.getExecutionType(), tickProcessor.getLimitPrice(stock.getTicker()))
            .subscribe(

                () -> {
                  logger.info("Trade complete for {}", stock);

                  thetasToReverse.stream()
                      .filter(theta -> theta.getStock().getId().equals(stock.getId()))
                      .map(DefaultPriceLevel::of)
                      .distinct()
                      .forEach(

                          priceLevel -> deleteMonitor(priceLevel));
                },

                exception -> {
                  logger.error("Error with Trade of {}", stock);
                }

        );

        tickManagerDisposables.add(disposableTrade);
      }
    }
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    tickSubscriber.unsubscribeAll();
    tickManagerDisposables.dispose();
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
