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
import theta.execution.domain.CandidateStockOrder;
import theta.tick.api.Tick;
import theta.tick.api.TickMonitor;
import theta.tick.api.TickProcessor;
import theta.util.MarketUtility;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
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
    private final MarketUtility marketUtility;

    private static final Duration TICK_DELAY_WARNING = Duration.ofMillis(1000L);

    private final List<Theta> monitoredThetas = new ArrayList<>();

    private final ManagerStatus managerStatus = ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

    private final Composite tickManagerDisposables = Disposables.composite();

    /**
     * Create TickManager using supplied Subscriber and Processor.
     *
     * @param tickSubscriber TickSubscriber to use for TickManager
     * @param tickProcessor  TickProcessor to use for TickManager
     */
    public TickManager(TickSubscriber tickSubscriber, TickProcessor tickProcessor, Executor executor, MarketUtility marketUtility) {
        getStatus().changeState(ManagerState.STARTING);
        this.tickSubscriber = Objects.requireNonNull(tickSubscriber, "Tick Subscriber cannot be null.");
        this.tickProcessor = Objects.requireNonNull(tickProcessor, "Tick Processor cannot be null.");
        this.executor = Objects.requireNonNull(executor, "Executor cannot be null.");
        this.marketUtility = Objects.requireNonNull(marketUtility, "Market Utility cannot be null.");
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
                    .filter(tickFilter -> marketUtility.isDuringMarketHours(tickFilter.getTimestamp()))
                    .subscribe(
                            this::processTick,
                            exception -> {
                                logger.error("Error in Tick Manager", exception);
                                emitter.error(exception);
                            },
                            () -> {
                                getStatus().changeState(ManagerState.SHUTDOWN);
                                emitter.success();
                            });
            tickManagerDisposables.add(tickSubscriberDisposable);
        });
    }

    @Override
    public void addMonitor(Theta theta) {
        monitoredThetas.add(theta);
        tickSubscriber.addPriceLevelMonitor(DefaultPriceLevel.of(theta), tickProcessor);
    }

    @Override
    public int deleteMonitor(Theta theta) {
        monitoredThetas.remove(theta);
        return tickSubscriber.removePriceLevelMonitor(DefaultPriceLevel.of(theta));
    }

    private void processTick(Tick tick) {
        logger.debug("Processing: {}", tick);

        if (tick.getTimestamp().isBefore(Instant.now().minus(TICK_DELAY_WARNING))) {
            logger.warn("Tick timestamp indicates tick is significantly delayed: {}", tick);
        }

        final List<Theta> tradesToCheck = monitoredThetas.stream()
                .filter(theta -> theta.getTicker().equals(tick.getTicker()))
                .collect(Collectors.toList());

        if (!tradesToCheck.isEmpty()) {
            logger.info("Received {} Position(s) from Position Provider: {}", tradesToCheck.size(), tradesToCheck);

            final List<Theta> thetasToReverse = tradesToCheck.stream()
                    .filter(theta -> tickProcessor.processTick(tick, DefaultPriceLevel.of(theta)))
                    .collect(Collectors.toList());

            // FIXME: This won't always correctly calculate limit price
            for (final Stock stock : StockUtil.consolidateStock(thetasToReverse)) {
                CandidateStockOrder candidateOrder = tickProcessor.getCandidateStockOrder(stock);

                final Disposable disposableTrade =
                        executor.reverseTrade(candidateOrder).subscribe(
                                null,
                                exception -> logger.error("Error with Trade of {}", stock),
                                () -> {
                                    logger.info("Trade complete for {}", stock);

                                    thetasToReverse.stream()
                                            .filter(theta -> theta.getStock().getId().equals(stock.getId()))
                                            .distinct().forEach(this::deleteMonitor);
                                }
                        );

                tickManagerDisposables.add(disposableTrade);
            }
        } else {
            logger.warn("Received {}, but no positions were provided. Attempting to convert to MARKET order.", tick);
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
