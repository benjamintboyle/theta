package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.TickType;
import com.ib.client.Types.MktDataType;
import com.ib.controller.ApiController.ITopMktDataHandler;
import theta.api.TickHandler;
import theta.tick.api.PriceLevel;
import theta.tick.api.TickConsumer;

public class IbLastTickHandler implements ITopMktDataHandler, TickHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TickConsumer tickConsumer;

  private final String ticker;
  private Double bidPrice = Double.MIN_VALUE;
  private Double askPrice = Double.MIN_VALUE;
  private Double lastPrice = Double.MIN_VALUE;
  private Double openPrice = Double.MIN_VALUE;
  private Double lowPrice = Double.MIN_VALUE;
  private Double highPrice = Double.MIN_VALUE;
  private Double haltedPrice = Double.MIN_VALUE;

  private ZonedDateTime lastTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

  private Integer bidSize = Integer.MIN_VALUE;
  private Integer askSize = Integer.MIN_VALUE;
  private Double closePrice = Double.MIN_VALUE;
  private Integer volume = Integer.MIN_VALUE;
  private int lastSize = Integer.MIN_VALUE;

  private Boolean isSnapshot;

  private final Set<Double> fallsBelow = new HashSet<Double>();
  private final Set<Double> risesAbove = new HashSet<Double>();

  public IbLastTickHandler(String ticker, TickConsumer tickConsumer) {
    this.ticker = ticker;
    this.tickConsumer = tickConsumer;
    logger.info("Built Interactive Brokers Tick Handler for: {}", ticker);
  }

  @Override
  public void tickPrice(TickType tickType, double price, int canAutoExecute) {
    logger.info(
        "Received Tick from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}",
        ticker, tickType, price, canAutoExecute);

    switch (tickType) {
      case BID:
        bidPrice = price;
        break;
      case ASK:
        askPrice = price;
        break;
      case LAST:
        lastPrice = price;
        priceTrigger(lastPrice);
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
    logger.info("Received Tick Size from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Size: {}", ticker,
        tickType, size);

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
    switch (tickType) {
      case LAST_TIMESTAMP:
        lastTime = Instant.ofEpochSecond(Long.parseLong(value)).atZone(ZoneId.systemDefault());
        logger.info("Received Tick String from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Value: {}",
            ticker, tickType, lastTime);
        break;
      default:
        logger.warn("'Tick String' not logged for: {} with value: {}", tickType, value);
        break;
    }
  }

  @Override
  public void marketDataType(MktDataType marketDataType) {
    logger.info("Received Market Data from Interactive Brokers servers - Ticker: {}, Market Date Type: {}", ticker,
        marketDataType);

    isSnapshot = marketDataType == MktDataType.Frozen;
  }

  @Override
  public void tickSnapshotEnd() {
    logger.info("Ticker: {}, Tick Snapshot End", ticker);
  }

  private void priceTrigger(Double price) {
    for (final Double priceToFallBelow : fallsBelow) {
      if (price < priceToFallBelow) {
        publishTickNotification();
      }
    }

    for (final Double priceToRiseAbove : risesAbove) {
      if (price > priceToRiseAbove) {
        publishTickNotification();
      }
    }
  }

  private void publishTickNotification() {
    tickConsumer.acceptTick(ticker);
  }

  @Override
  public String getTicker() {
    return ticker;
  }

  @Override
  public Double getBid() {
    return bidPrice;
  }

  @Override
  public Double getAsk() {
    return askPrice;
  }

  @Override
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

  @Override
  public ZonedDateTime getLastTime() {
    return lastTime;
  }

  @Override
  public Integer getBidSize() {
    return bidSize;
  }

  @Override
  public Integer getAskSize() {
    return askSize;
  }

  @Override
  public Double getClose() {
    return closePrice;
  }

  @Override
  public Integer getVolume() {
    return volume;
  }

  public Integer getLastSize() {
    return lastSize;
  }

  @Override
  public Boolean isSnapshot() {
    return isSnapshot;
  }

  @Override
  public Integer addPriceLevel(PriceLevel priceLevel) {

    if (priceLevel.getTicker().equals(ticker)) {

      logger.info("Adding Price Level: {} ${} to Monitor: {}", priceLevel.tradeIf(), priceLevel.getStrikePrice(),
          priceLevel.getTicker());

      switch (priceLevel.tradeIf()) {
        case FALLS_BELOW:
          fallsBelow.add(priceLevel.getStrikePrice());
          break;
        case RISES_ABOVE:
          risesAbove.add(priceLevel.getStrikePrice());
          break;
        default:
          logger.error("Unknown Price Direction: {} for Price Level: {}", priceLevel.tradeIf(), priceLevel);
      }
    } else {
      logger.error("Attempted to add PriceLevel for '{}' to '{}' Monitor", priceLevel.getTicker(), ticker);
    }

    logPriceLevels();

    return fallsBelow.size() + risesAbove.size();
  }

  @Override
  public Integer removePriceLevel(PriceLevel priceLevel) {
    logger.info("Removing Price Level '{}' from Tick Handler: {}", priceLevel, ticker);
    if (!priceLevel.getTicker().equals(ticker)) {
      logger.error("Attempted to remove PriceLevel for '{}' from '{}' Monitor", priceLevel.getTicker(), ticker);
    }

    logger.info("Removing Price Level {} ${} from Monitor: {}", priceLevel.tradeIf(), priceLevel.getStrikePrice(),
        priceLevel.getTicker());

    switch (priceLevel.tradeIf()) {
      case FALLS_BELOW:
        if (!fallsBelow.remove(priceLevel.getStrikePrice())) {
          logger.warn("No Price Level to remove for {} ${} from Monitor: {}", priceLevel.tradeIf(),
              priceLevel.getStrikePrice(), priceLevel.getTicker());
        }
        break;
      case RISES_ABOVE:
        if (!risesAbove.remove(priceLevel.getStrikePrice())) {
          logger.warn("No Price Level to remove for {} ${} from Monitor: {}", priceLevel.tradeIf(),
              priceLevel.getStrikePrice(), priceLevel.getTicker());
        }
        break;
      default:
        logger.error("Unknown Price Direction: {}", priceLevel.tradeIf());
    }

    logPriceLevels();

    return fallsBelow.size() + risesAbove.size();
  }

  private void logPriceLevels() {
    logger.info("Price Levels for '{}': FALLS_BELOW={}, RISES_ABOVE={}", ticker, fallsBelow, risesAbove);
  }
}
