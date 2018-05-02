package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.TickType;
import com.ib.client.Types.MktDataType;
import com.ib.controller.ApiController.ITopMktDataHandler;
import brokers.interactive_brokers.util.IbTickUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import theta.ThetaSchedulersFactory;
import theta.api.TickHandler;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;
import theta.util.ThetaMarketUtil;

public class IbTickHandler implements ITopMktDataHandler, TickHandler {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Subject<TickType> tickSubject = PublishSubject.create();

  private final Ticker ticker;
  private final TickProcessor tickProcessor;

  private double bidPrice = -1.0;
  private double askPrice = -1.0;
  private double lastPrice = -1.0;

  private ZonedDateTime bidTime = ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE);
  private ZonedDateTime askTime = ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE);
  private ZonedDateTime lastTime = ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE);

  private final Set<PriceLevel> priceLevels = new HashSet<>();

  private final CompositeDisposable tickHandlerDisposables = new CompositeDisposable();

  private Supplier<String> lazyToString = this::toString;

  public IbTickHandler(Ticker ticker, TickProcessor tickProcessor) {
    this.ticker = Objects.requireNonNull(ticker, "Ticker cannot be null for Tick Processor initialization.");
    this.tickProcessor =
        Objects.requireNonNull(tickProcessor, "Ticker Processor cannot be null for Tick Processor initialization.");

    logger.info("Built Interactive Brokers Tick Handler for: {}", ticker);
  }

  @Override
  public Flowable<Tick> getTicks() {

    return tickSubject.toFlowable(BackpressureStrategy.LATEST)
        // Don't let thread out of IB packages
        .observeOn(ThetaSchedulersFactory.computeThread())
        // Determine if tick is applicable for tick processor
        .filter(tickType -> tickProcessor.isApplicable(IbTickUtil.convertToEngineTickType(tickType)))
        // Build tick with latest data
        .map(this::buildTick)
        // Process tick to see if it should be propagated up to manager
        .filter(this::processTick);
  }

  @Override
  public void tickPrice(TickType tickType, double price, int canAutoExecute) {
    logger.info(
        "Received Tick from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}",
        getTicker(), tickType, price, canAutoExecute);

    switch (tickType) {
      case BID:
        bidTime = ZonedDateTime.now(ThetaMarketUtil.MARKET_TIMEZONE);
        bidPrice = price;
        tickSubject.onNext(tickType);
        break;
      case ASK:
        askTime = ZonedDateTime.now(ThetaMarketUtil.MARKET_TIMEZONE);
        askPrice = price;
        tickSubject.onNext(tickType);
        break;
      case LAST:
        lastPrice = price;
        tickSubject.onNext(tickType);
        break;
      case CLOSE:
        break;
      case OPEN:
        break;
      case LOW:
        break;
      case HIGH:
        break;
      case HALTED:
        break;
      default:
        logger.warn("'Tick Price' not logged for: {} @ {}", tickType, price);
        break;
    }
  }

  @Override
  public void tickSize(TickType tickType, int size) {
    logger.debug("Received Tick Size from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Size: {}",
        getTicker(), tickType, size);
  }

  @Override
  public void tickString(TickType tickType, String value) {

    logger.debug("Received Tick String from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Value: {}",
        getTicker(), tickType, value);

    switch (tickType) {
      case LAST_TIMESTAMP:
        lastTime = Instant.ofEpochSecond(Long.parseLong(value)).atZone(ThetaMarketUtil.MARKET_TIMEZONE);
        break;
      case BID_EXCH:
        break;
      case ASK_EXCH:
        break;
      default:
        logger.warn("'Tick String' not logged for: {} with value: {}", tickType, value);
        break;
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

  private Tick buildTick(TickType tickType) {
    return new DefaultTick(getTicker(), IbTickUtil.convertToEngineTickType(tickType), getLast(), getBid(), getAsk(),
        getTickTime(tickType));
  }

  private boolean processTick(Tick tick) {
    return priceLevels.stream().anyMatch(priceLevel -> tickProcessor.processTick(tick, priceLevel));
  }

  @Override
  public Ticker getTicker() {
    return ticker;
  }

  private double getBid() {
    return bidPrice;
  }

  private double getAsk() {
    return askPrice;
  }

  private double getLast() {
    return lastPrice;
  }

  private ZonedDateTime getTickTime(TickType tickType) {

    ZonedDateTime tickTime = ZonedDateTime.now(ThetaMarketUtil.MARKET_TIMEZONE);

    switch (tickType) {
      case LAST:
        tickTime = lastTime;
        break;
      case BID:
        tickTime = bidTime;
        break;
      case ASK:
        tickTime = askTime;
        break;
      default:
        logger.warn("When determining TickTime, could not determine TickType of {}. Using time now.", tickType);
    }

    return tickTime;
  }

  @Override
  public Integer addPriceLevelMonitor(PriceLevel priceLevel) {

    if (priceLevel.getTicker().equals(getTicker())) {
      logger.info("Adding Price Level: {} ${} to Tick Handler: {}", priceLevel.tradeIf(), priceLevel.getPrice(),
          lazyToString);

      priceLevels.add(priceLevel);

    } else {
      logger.error("Attempted to add PriceLevel for '{}' to '{}' Monitor", priceLevel.getTicker(), getTicker());
    }

    return priceLevels.size();
  }

  @Override
  public Integer removePriceLevelMonitor(PriceLevel priceLevel) {

    if (priceLevel.getTicker().equals(getTicker())) {

      logger.info("Removing Price Level {} ${} from Tick Handler: {}", priceLevel.tradeIf(), priceLevel.getPrice(),
          lazyToString);

      if (!priceLevels.remove(priceLevel)) {
        logger.warn("Attempted to remove non-existant Price Level: {} from Tick Handler: {}", priceLevel, lazyToString);
      }

      // Wait a second and then check if there are no price levels to monitor. Cancel if price level count
      // is 0.
      if (priceLevels.isEmpty()) {

        Disposable cancelDisposable =
            Completable.timer(1L, TimeUnit.SECONDS, ThetaSchedulersFactory.ioThread()).subscribe(

                () -> {
                  if (priceLevels.isEmpty()) {
                    logger.debug("Unsubscribing Tick Handler: {}", toString());
                    cancel();
                  }
                },

                exception -> logger.error("Issue with cancelling Tick Handler", exception));

        tickHandlerDisposables.add(cancelDisposable);
      }

    } else {
      logger.error("Attempted to remove PriceLevel for '{}' from Tick Handler: {}", priceLevel.getTicker(),
          lazyToString);
    }

    return priceLevels.size();
  }

  @Override
  public Set<PriceLevel> getPriceLevelsMonitored() {

    return priceLevels;
  }

  @Override
  public void cancel() {

    if (!tickSubject.hasComplete()) {
      tickSubject.onComplete();
    }

    if (!tickHandlerDisposables.isDisposed()) {
      tickHandlerDisposables.dispose();
    }

  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(getTicker());
    builder.append(" ");

    List<Double> fallsBelow = priceLevels.stream()
        .filter(priceLevel -> priceLevel.tradeIf() == PriceLevelDirection.FALLS_BELOW)
        .map(PriceLevel::getPrice)
        .collect(Collectors.toList());

    List<Double> risesAbove = priceLevels.stream()
        .filter(priceLevel -> priceLevel.tradeIf() == PriceLevelDirection.RISES_ABOVE)
        .map(PriceLevel::getPrice)
        .collect(Collectors.toList());

    if (!fallsBelow.isEmpty()) {
      builder.append(PriceLevelDirection.FALLS_BELOW);
      builder.append("=");
      builder.append(fallsBelow);

      if (!risesAbove.isEmpty()) {
        builder.append(" ");
      }
    }

    if (!risesAbove.isEmpty()) {
      builder.append(PriceLevelDirection.RISES_ABOVE);
      builder.append("=");
      builder.append(risesAbove);
    }

    return builder.toString();
  }

}
