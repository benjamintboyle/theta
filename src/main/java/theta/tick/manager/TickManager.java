package theta.tick.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import theta.api.TickSubscriber;
import theta.domain.composed.Theta;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;
import theta.domain.pricelevel.DefaultPriceLevel;
import theta.domain.stock.Stock;
import theta.domain.util.StockUtil;
import theta.execution.api.Executor;
import theta.tick.api.Tick;
import theta.tick.api.TickMonitor;
import theta.tick.api.TickProcessor;
import theta.util.ThetaMarketUtil;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TickManager implements TickMonitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TickSubscriber tickSubscriber;
    private final TickProcessor tickProcessor;
    private final Executor executor;

    private final List<Theta> monitoredThetas = new ArrayList<>();

    private final ManagerStatus managerStatus =
            ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

    private final Composite tickManagerDisposables = Disposables.composite();

    /**
     * Create TickManager using supplied Subscriber and Processor.
     *
     * @param tickSubscriber TickSubscriber to use for TickManager
     * @param tickProcessor  TickProcessor to use for TickManager
     */
    public TickManager(TickSubscriber tickSubscriber, TickProcessor tickProcessor,
                       Executor executor) {
        getStatus().changeState(ManagerState.STARTING);
        this.tickSubscriber = Objects.requireNonNull(tickSubscriber, "Tick Subscriber cannot be null.");
        this.tickProcessor = Objects.requireNonNull(tickProcessor, "Tick Processor cannot be null.");
        this.executor = Objects.requireNonNull(executor, "Executor cannot be null.");
    }

    /**
     * Starts processing Ticks.
     *
     * @return Completable for functionally processing Ticks
     */
    public Mono<Void> startTickProcessing() {

        logger.debug("Starting Tick Processing");
        getStatus().changeState(ManagerState.RUNNING);

        return Mono.create(emitter -> {

            final Disposable tickSubscriberDisposable = tickSubscriber.getTicksAcrossStrikePrices()
                    // Determine if time now is during market hours
                    .filter(tickFilter -> ThetaMarketUtil.isDuringNewYorkMarketHours(Instant.now()))
                    .subscribe(

                            this::processTick,

                            exception -> logger.error("Error in Tick Manager", exception),

                            () -> getStatus().changeState(ManagerState.SHUTDOWN));

            tickManagerDisposables.add(tickSubscriberDisposable);
        });
    }

    @Override
    public void addMonitor(Theta theta) {
        monitoredThetas.add(theta);
        tickSubscriber.addPriceLevelMonitor(DefaultPriceLevel.of(theta), tickProcessor);
    }

    @Override
    public Integer deleteMonitor(Theta theta) {
        monitoredThetas.remove(theta);
        return tickSubscriber.removePriceLevelMonitor(DefaultPriceLevel.of(theta));
    }

    private void processTick(Tick tick) {

        logger.debug("Processing: {}", tick);

        if (tick.getTimestamp().isBefore(Instant.now().minusSeconds(2))) {
            logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
        }

        final List<Theta> tradesToCheck = monitoredThetas.stream()
                .filter(theta -> theta.getTicker().equals(tick.getTicker())).collect(Collectors.toList());

        if (!tradesToCheck.isEmpty()) {

            logger.info("Received {} Positions from Position Provider: {}", tradesToCheck.size(),
                    tradesToCheck);

            final List<Theta> thetasToReverse = tradesToCheck.stream()
                    .filter(theta -> tickProcessor.processTick(tick, DefaultPriceLevel.of(theta)))
                    .collect(Collectors.toList());

            // FIXME: This doesn't correctly calculate limit price
            for (final Stock stock : StockUtil.consolidateStock(thetasToReverse)) {

                final Disposable disposableTrade =
                        executor.reverseTrade(stock, tickProcessor.getExecutionType(),
                                tickProcessor.getLimitPrice(stock.getTicker())).subscribe(

                                (Void) -> {
                                    logger.info("Trade complete for {}", stock);

                                    thetasToReverse.stream()
                                            .filter(theta -> theta.getStock().getId().equals(stock.getId()))
                                            .distinct().forEach(

                                            this::deleteMonitor);
                                },

                                exception -> logger.error("Error with Trade of {}", stock)

                        );

                tickManagerDisposables.add(disposableTrade);
            }
        } else {
            // If we are still getting ticks, but there are no positions provided, assume partial order
            // fill and convert rest to market order
            logger.warn(
                    "Received Tick, but no positions were provided. Attempting to convert to MARKET order.");
            executor.convertToMarketOrderIfExists(tick.getTicker());
        }
    }

    public ManagerStatus getStatus() {
        return managerStatus;
    }

    @Override
    public void shutdown() {
        getStatus().changeState(ManagerState.STOPPING);
        tickSubscriber.unsubscribeAll();
        tickManagerDisposables.dispose();
    }

}
