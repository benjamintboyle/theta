package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.TickType;
import com.ib.client.Types.MktDataType;
import com.ib.controller.ApiController.ITopMktDataHandler;
import brokers.interactive_brokers.util.IbTickUtil;
import theta.api.TickHandler;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.tick.api.Tick;
import theta.tick.api.TickConsumer;
import theta.tick.domain.DefaultTick;
import theta.tick.processor.TickProcessor;

public class IbTickHandler implements ITopMktDataHandler, TickHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickConsumer tickConsumer;

  private final Ticker ticker;
  private final TickProcessor tickProcessor;

  private Double bidPrice = Double.MIN_VALUE;
  private Double askPrice = Double.MIN_VALUE;
  private Double lastPrice = Double.MIN_VALUE;
  private Double openPrice = Double.MIN_VALUE;
  private Double lowPrice = Double.MIN_VALUE;
  private Double highPrice = Double.MIN_VALUE;
  private Double haltedPrice = Double.MIN_VALUE;

  private ZonedDateTime lastTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
  private String bidExchange = "";
  private String askExchange = "";

  private Integer bidSize = Integer.MIN_VALUE;
  private Integer askSize = Integer.MIN_VALUE;
  private Double closePrice = Double.MIN_VALUE;
  private Integer volume = Integer.MIN_VALUE;
  private int lastSize = Integer.MIN_VALUE;

  private Boolean isSnapshot;

  private Tick latestTick;

  private final Set<PriceLevel> priceLevels = new HashSet<>();

  public IbTickHandler(Ticker ticker, TickProcessor tickProcessor, TickConsumer tickConsumer) {

    this.ticker = ticker;
    this.tickProcessor = tickProcessor;
    this.tickConsumer = tickConsumer;

    logger.info("Built Interactive Brokers Tick Handler for: {}", this.ticker);
  }

  @Override
  public void tickPrice(TickType tickType, double price, int canAutoExecute) {
    logger.info(
        "Received Tick from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}",
        getTicker(), tickType, price, canAutoExecute);

    switch (tickType) {
      case BID:
        bidPrice = price;
        checkTick(tickType);
        break;
      case ASK:
        askPrice = price;
        checkTick(tickType);
        break;
      case LAST:
        lastPrice = price;
        checkTick(tickType);
        break;
      case CLOSE:
        closePrice = price;
        break;
      case OPEN:
        openPrice = price;
        break;
      case LOW:
        lowPrice = price;
        break;
      case HIGH:
        highPrice = price;
        break;
      case HALTED:
        haltedPrice = price;
        break;
      default:
        logger.warn("'Tick Price' not logged for: {} @ {}", tickType, price);
        break;
    }
  }

  @Override
  public void tickSize(TickType tickType, int size) {
    logger.info("Received Tick Size from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Size: {}",
        getTicker(), tickType, size);

    switch (tickType) {
      case BID_SIZE:
        bidSize = size;
        break;
      case ASK_SIZE:
        askSize = size;
        break;
      case VOLUME:
        volume = size;
        break;
      case LAST_SIZE:
        lastSize = size;
        break;
      default:
        logger.warn("'Tick Size' not logged for: {} with size: {}", tickType, size);
        break;
    }
  }

  @Override
  public void tickString(TickType tickType, String value) {

    logger.info("Received Tick String from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Value: {}",
        getTicker(), tickType, value);

    switch (tickType) {
      case LAST_TIMESTAMP:
        lastTime = Instant.ofEpochSecond(Long.parseLong(value)).atZone(ZoneId.systemDefault());
        break;
      case BID_EXCH:
        bidExchange = value;
        break;
      case ASK_EXCH:
        askExchange = value;
        break;
      default:
        logger.warn("'Tick String' not logged for: {} with value: {}", tickType, value);
        break;
    }
  }

  @Override
  public void marketDataType(MktDataType marketDataType) {
    logger.info("Received Market Data from Interactive Brokers servers - Ticker: {}, Market Date Type: {}", getTicker(),
        marketDataType);

    isSnapshot = marketDataType == MktDataType.Frozen;
  }

  @Override
  public void tickSnapshotEnd() {
    logger.info("Ticker: {}, Tick Snapshot End", getTicker());
  }

  private void checkTick(TickType tickType) {

    if (tickProcessor.isApplicable(IbTickUtil.convertToEngineTickType(tickType))) {

      latestTick = new DefaultTick(getTicker(), IbTickUtil.convertToEngineTickType(tickType), getLast(), getBid(),
          getAsk(), getLastTime());

      priceLevels.stream().filter(priceLevel -> tickProcessor.process(latestTick, priceLevel)).findAny().ifPresent(
          priceLevel -> publishTickNotification());
    }
  }

  private void publishTickNotification() {
    tickConsumer.acceptTick(getTicker());
  }

  public Ticker getTicker() {
    return ticker;
  }

  public Double getBid() {
    return bidPrice;
  }

  public Double getAsk() {
    return askPrice;
  }

  public Double getLast() {
    return lastPrice;
  }

  public Double getOpenPrice() {
    return openPrice;
  }

  public Double getLowPrice() {
    return lowPrice;
  }

  public Double getHighPrice() {
    return highPrice;
  }

  public Double getHaltedPrice() {
    return haltedPrice;
  }

  public ZonedDateTime getLastTime() {
    return lastTime;
  }

  public String getBidExchange() {
    return bidExchange;
  }

  public String getAskExchange() {
    return askExchange;
  }

  public Integer getBidSize() {
    return bidSize;
  }

  public Integer getAskSize() {
    return askSize;
  }

  public Double getClose() {
    return closePrice;
  }

  public Integer getVolume() {
    return volume;
  }

  public Integer getLastSize() {
    return lastSize;
  }

  public Boolean isSnapshot() {
    return isSnapshot;
  }

  @Override
  public Tick getLatestTick() {
    return latestTick;
  }

  @Override
  public Integer addPriceLevelMonitor(PriceLevel priceLevel) {

    if (priceLevel.getTicker().equals(getTicker())) {

      logger.info("Adding Price Level: {} ${} to Tick Handler: {}", priceLevel.tradeIf(), priceLevel.getPrice(),
          getTicker());

      priceLevels.add(priceLevel);

    } else {
      logger.error("Attempted to add PriceLevel for '{}' to '{}' Monitor", priceLevel.getTicker(), getTicker());
    }

    logger.info(toString());

    return priceLevels.size();
  }

  @Override
  public Integer removePriceLevelMonitor(PriceLevel priceLevel) {

    if (priceLevel.getTicker().equals(getTicker())) {

      logger.info("Removing Price Level {} ${} from Tick Handler: {}", priceLevel.tradeIf(), priceLevel.getPrice(),
          getTicker());

      if (!priceLevels.remove(priceLevel)) {
        logger.warn("Attempted to remove non-existant Price Level: {} from Tick Handler: {}", priceLevel, getTicker());
      }
    } else {
      logger.error("Attempted to remove PriceLevel for '{}' from '{}' Monitor", priceLevel.getTicker(), getTicker());
    }

    logger.info(toString());

    return priceLevels.size();
  }

  @Override
  public Set<PriceLevel> getPriceLevelsMonitored() {

    return priceLevels;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Tick Handler Price Levels for '");
    builder.append(getTicker());
    builder.append(": ");

    builder.append(PriceLevelDirection.FALLS_BELOW);
    builder.append(" [ ");
    builder.append(priceLevels.stream()
        .filter(priceLevel -> priceLevel.tradeIf() == PriceLevelDirection.FALLS_BELOW)
        .map(PriceLevel::getPrice)
        .collect(Collectors.toList()));
    builder.append(" ]");

    builder.append(PriceLevelDirection.RISES_ABOVE);
    builder.append(" [ ");
    builder.append(priceLevels.stream()
        .filter(priceLevel -> priceLevel.tradeIf() == PriceLevelDirection.RISES_ABOVE)
        .map(PriceLevel::getPrice)
        .collect(Collectors.toList()));
    builder.append(" ]");

    return builder.toString();
  }

}
