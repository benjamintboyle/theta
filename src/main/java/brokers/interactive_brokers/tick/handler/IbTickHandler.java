package brokers.interactive_brokers.tick;

import brokers.interactive_brokers.util.IbTickUtil;
import com.ib.client.TickType;
import com.ib.client.Types.MktDataType;
import com.ib.controller.ApiController.ITopMktDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.ReplayProcessor;
import theta.api.TickHandler;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class IbTickHandler implements ITopMktDataHandler, TickHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FluxProcessor<TickType, TickType> tickSubject = ReplayProcessor.createSizeAndTimeout(1, Duration.ofSeconds(1L));

    private final Ticker ticker;
    private final TickProcessor tickProcessor;
    private final Map<TickType, IbTickInstant> instantTick = new HashMap<>();
    private Instant lastTime = Instant.EPOCH;

    private final Set<PriceLevel> priceLevels = new HashSet<>();

    /**
     * Create Interactive Brokers tick handler for a specific symbol. And with a specific processor
     * type.
     *
     * @param ticker        Text version of symbol.
     * @param tickProcessor Processor that will handle transition of ticker symbol.
     */
    public IbTickHandler(Ticker ticker, TickProcessor tickProcessor) {
        this.ticker = Objects.requireNonNull(ticker, "Ticker cannot be null for Tick Processor initialization.");
        this.tickProcessor = Objects.requireNonNull(tickProcessor, "Ticker Processor cannot be null for Tick Processor initialization.");

        instantTick.put(TickType.LAST, new DefaultIbTickInstant(TickType.LAST));
        instantTick.put(TickType.BID, new DefaultIbTickInstant(TickType.BID));
        instantTick.put(TickType.ASK, new DefaultIbTickInstant(TickType.ASK));

        logger.info("Built Interactive Brokers Tick Handler for: {}", ticker);
    }

    @Override
    public Flux<Tick> getTicks() {
        return tickSubject.onBackpressureLatest()
                .map(this::buildTick)
                .filter(this::processTick);
    }

    @Override
    public void tickPrice(TickType tickType, double price, int canAutoExecute) {
        logger.debug("Received Tick Price from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}",
                getTicker(), tickType, price, canAutoExecute);

        switch (tickType) {
            case BID, ASK -> {
                instantTick.get(tickType).updatePriceTime(price, Instant.now());
                addTickIfApplicable(tickType);
            }
            case LAST -> {
                instantTick.get(tickType).updatePriceTime(price, lastTime);
                addTickIfApplicable(tickType);
            }
            case CLOSE, OPEN, LOW, HIGH, HALTED -> logger.debug("TickType: {} not implemented", tickType);
            default -> logger.warn("'Tick Price' not logged for: {} @ {}", tickType, price);
        }
    }

    @Override
    public void tickSize(TickType tickType, int size) {
        logger.debug(
                "Received Tick Size from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Size: {}",
                getTicker(), tickType, size);
    }

    @Override
    public void tickString(TickType tickType, String value) {
        logger.debug("Received Tick String from Interactive Brokers servers - Ticker: {}, Tick Type: {}, "
                + "Value: {}", getTicker(), tickType, value);

        switch (tickType) {
            case LAST_TIMESTAMP -> lastTime = Instant.ofEpochSecond(Long.parseLong(value));
            case BID_EXCH, ASK_EXCH -> logger.debug("TickType: {} not implemented", tickType);
            default -> logger.warn("'Tick String' not logged for: {} with value: {}", tickType, value);
        }
    }

    @Override
    public void marketDataType(MktDataType marketDataType) {
        logger.debug("Received Market Data from Interactive Brokers servers - Ticker: {}, Market Date Type: {}",
                getTicker(), marketDataType);
    }

    @Override
    public void tickSnapshotEnd() {
        logger.debug("Ticker: {}, Tick Snapshot End", getTicker());
    }

    @Override
    public Ticker getTicker() {
        return ticker;
    }

    private Tick buildTick(TickType tickType) {
        return new DefaultTick(getTicker(),
                IbTickUtil.convertToEngineTickType(tickType),
                getTickPrice(TickType.LAST),
                getTickPrice(TickType.BID),
                getTickPrice(TickType.ASK),
                getTickTime(tickType));
    }

    private boolean processTick(Tick tick) {
        if (priceLevels.isEmpty()) {
            logger.warn("Attempted to process Tick when Tick Handler has no Price Levels. Tick: {}, TickHandler: {}", tick, this);
        }

        return priceLevels.stream().anyMatch(priceLevel -> tickProcessor.processTick(tick, priceLevel));
    }

    private void addTickIfApplicable(TickType tickType) {
        if (tickProcessor.isApplicable(IbTickUtil.convertToEngineTickType(tickType))) {
            tickSubject.onNext(tickType);
        }
    }

    private double getTickPrice(TickType tickType) {
        return instantTick.get(tickType).getInstancePrice();
    }

    private Instant getTickTime(TickType tickType) {
        return instantTick.get(tickType).getInstantTime();
    }

    @Override
    public int addPriceLevelMonitor(PriceLevel priceLevel) {
        if (priceLevel.getTicker().equals(getTicker())) {
            logger.info("Adding Price Level: {} ${} to Tick Handler: {}", priceLevel.tradeIf(), priceLevel.getPrice(), this);
            priceLevels.add(priceLevel);

        } else {
            logger.error("Mismatched Tickers. Attempted to add Price Level: {} to Tick Handler: {} Monitor", priceLevel, this);
        }

        return priceLevels.size();
    }

    @Override
    public int removePriceLevelMonitor(PriceLevel priceLevel) {
        if (priceLevel.getTicker().equals(getTicker())) {
            logger.info("Removing Price Level {} ${} from Tick Handler: {}",
                    priceLevel.tradeIf(), priceLevel.getPrice(), this);

            if (!priceLevels.remove(priceLevel)) {
                logger.warn("Attempted to remove non-existant Price Level: {} from Tick Handler: {}",
                        priceLevel, this);
            }

            if (priceLevels.isEmpty()) {
                logger.debug("Unsubscribing Tick Handler: {}", this);
                cancel();
            }
        } else {
            logger.error("Attempted to remove PriceLevel for '{}' from Tick Handler: {}",
                    priceLevel.getTicker(), this);
        }

        return priceLevels.size();
    }

    @Override
    public Set<PriceLevel> getPriceLevelsMonitored() {
        return priceLevels;
    }

    @Override
    public void cancel() {
        if (!tickSubject.hasCompleted()) {
            logger.debug("Completing Tick Subject");
            tickSubject.onComplete();
        } else {
            logger.warn("Tried to complete Tick Subject when it is already completed.");
        }
    }

    @Override
    public String toString() {
        return "IbTickHandler{" +
                "tickSubject=" + tickSubject +
                ", ticker=" + ticker +
                ", tickProcessor=" + tickProcessor +
                ", instantTick=" + instantTick +
                ", lastTime=" + lastTime +
                ", priceLevels=" + priceLevels +
                '}';
    }
}
