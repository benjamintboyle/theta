package brokers.interactive_brokers.tick;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.tick.handler.IbTickHandler;
import brokers.interactive_brokers.tick.handler.IbTickHandlerFactory;
import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.contracts.StkContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class IbTickSubscriber implements TickSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IbController ibController;
    private final IbTickHandlerFactory tickHandlerFactory;
    private final Sinks.Many<Tick> tickSink = Sinks.many().multicast().onBackpressureBuffer();
    private final ConcurrentMap<Ticker, IbTickHandler> ibTickHandlers = new ConcurrentHashMap<>();
    private final Disposable.Composite disposables = Disposables.composite();

    public IbTickSubscriber(IbController ibController, IbTickHandlerFactory tickHandlerFactory) {
        logger.info("Starting Interactive Brokers Tick Subscriber");
        this.ibController = ibController;
        this.tickHandlerFactory = tickHandlerFactory;
    }

    @Override
    public Flux<Tick> getTicksAcrossStrikePrices() {
        return tickSink.asFlux();
    }

    @Override
    public void addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor) {
        getOrCreateTickHandler(priceLevel.getTicker(), tickProcessor).addPriceLevelMonitor(priceLevel);
        logTickHandlers();

        getPriceLevelsMonitored(priceLevel.getTicker());
    }

    @Override
    public int removePriceLevelMonitor(PriceLevel priceLevel) {
        getTickHandler(priceLevel.getTicker()).ifPresentOrElse(
                ibLastTickHandler -> ibLastTickHandler.removePriceLevelMonitor(priceLevel),
                () -> logger.warn("Cannot remove Price Level. IB Last Tick Handler does not exist for {}", priceLevel.getTicker())
        );
        logTickHandlers();

        return getPriceLevelsMonitored(priceLevel.getTicker()).size();
    }

    private Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker) {
        return getTickHandler(ticker).map(TickHandler::getPriceLevelsMonitored).orElse(Set.of());
    }

    @Override
    public void unsubscribeAll() {
        for (final Ticker ticker : ibTickHandlers.keySet()) {
            unsubscribeTick(ticker);
        }
        if (!tickSink.isScanAvailable()) {
            tickSink.tryEmitComplete().orThrow();
        }
        disposables.dispose();
    }

    private Optional<TickHandler> getTickHandler(Ticker ticker) {
        return Optional.ofNullable(ibTickHandlers.get(ticker));
    }

    private TickHandler getOrCreateTickHandler(Ticker ticker, TickProcessor tickProcessor) {
        if (!ibTickHandlers.containsKey(ticker)) {
            logger.info("Subscribing to Ticks for: {}", ticker);

            IbTickHandler ibTickHandler = tickHandlerFactory.createTickHandler(ticker, tickProcessor);
            ibTickHandlers.put(ticker, ibTickHandler);
            StkContract contract = new StkContract(ticker.getSymbol());

            logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
                    IbStringUtil.toStringContract(contract));
            ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

            Disposable disposableTickHandler = ibTickHandler.getTicks()
                    .doOnError(exception -> logger.error("Error with: {}", ibTickHandler, exception))
                    .doOnComplete(() -> {
                        logger.info("Tick Handler completed and unsubscribed for: {}", ticker);
                        unsubscribeTick(ticker);
                    }).subscribe(
                            tickSink::tryEmitNext,
                            tickSink::tryEmitError,
                            tickSink::tryEmitComplete
                    );

            disposables.add(disposableTickHandler);
        }

        return ibTickHandlers.get(ticker);
    }

    private void unsubscribeTick(Ticker ticker) {
        final IbTickHandler ibTickHandler = ibTickHandlers.remove(ticker);

        if (ibTickHandler != null) {
            logger.info("Unsubscribing from Tick Handler: {}", ibTickHandler);
            ibController.getController().cancelTopMktData(ibTickHandler);
            ibTickHandler.cancel();
        } else {
            logger.warn("IB Last Tick Handler does not exist for: {}", ticker);
        }
    }

    private void logTickHandlers() {
        logger.info("Current Handlers: {}", ibTickHandlers.values().stream()
                .sorted(Comparator.comparing(TickHandler::getTicker)).collect(Collectors.toList()));
    }
}
