package theta.tick.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Ticker;
import theta.tick.api.Tick;

public class LastTick implements Tick {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Ticker ticker;
  private final TickType type = TickType.LAST;
  private final Double lastPrice;
  private final Double bidPrice;
  private final Double askPrice;
  private final ZonedDateTime timestamp;

  public LastTick(final Ticker ticker, final Double lastPrice, final Double bidPrice, final Double askPrice,
      final ZonedDateTime timestamp) {
    this.ticker = ticker;
    this.lastPrice = lastPrice;
    this.bidPrice = bidPrice;
    this.askPrice = askPrice;
    this.timestamp = timestamp;

    logger.debug("Built: {}", toString());
  }

  public Double getPrice() {

    Double price;

    switch (type) {
      case LAST:
        price = getLastPrice();
        break;
      case BID:
        price = getBidPrice();
        break;
      case ASK:
        price = getAskPrice();
        break;
      default:
        throw new IllegalArgumentException("Expected Tick to be of type LAST, BID, or ASK, but was " + type);
    }

    return price;
  }

  public Double getLastPrice() {
    return lastPrice;
  }

  public Double getBidPrice() {
    return bidPrice;
  }

  public Double getAskPrice() {
    return askPrice;
  }

  public Ticker getTicker() {
    return ticker;
  }

  public TickType getTickType() {
    return type;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Tick [");
    builder.append("Ticker: ");
    builder.append(getTicker());
    builder.append(", Price: ");
    builder.append(getLastPrice());
    builder.append(", Time: ");
    builder.append(getTimestamp());
    builder.append(", Type: ");
    builder.append(getTickType());

    builder.append("]");

    return builder.toString();
  }
}
