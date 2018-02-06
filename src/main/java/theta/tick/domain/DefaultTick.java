package theta.tick.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Ticker;
import theta.tick.api.Tick;

public class DefaultTick implements Tick {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Ticker ticker;
  private final TickType type;
  private final Double lastPrice;
  private final Double bidPrice;
  private final Double askPrice;
  private final ZonedDateTime timestamp;

  public DefaultTick(final Ticker ticker, final TickType type, final Double lastPrice, final Double bidPrice,
      final Double askPrice, final ZonedDateTime timestamp) {
    this.ticker = ticker;
    this.type = type;
    this.lastPrice = lastPrice;
    this.bidPrice = bidPrice;
    this.askPrice = askPrice;
    this.timestamp = timestamp;

    logger.debug("Built: {}", toString());
  }

  @Override
  public Double getLastPrice() {
    return lastPrice;
  }

  @Override
  public Double getBidPrice() {
    return bidPrice;
  }

  @Override
  public Double getAskPrice() {
    return askPrice;
  }

  @Override
  public Ticker getTicker() {
    return ticker;
  }

  @Override
  public TickType getTickType() {
    return type;
  }

  @Override
  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Tick [");
    builder.append("Ticker: ");
    builder.append(getTicker());
    builder.append(", Last Price: ");
    builder.append(getLastPrice());
    builder.append(", Bid Price: ");
    builder.append(getBidPrice());
    builder.append(", Ask Price: ");
    builder.append(getAskPrice());
    builder.append(", Time: ");
    builder.append(getTimestamp());
    builder.append(", Type: ");
    builder.append(getTickType());

    builder.append("]");

    return builder.toString();
  }
}
