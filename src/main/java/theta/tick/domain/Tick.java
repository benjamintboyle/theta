package theta.tick.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Ticker;

public class Tick {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Double price;
  private final Ticker ticker;
  private final ZonedDateTime timestamp;
  private final TickType type;

  public Tick(final Ticker ticker, final Double price, final TickType type, final ZonedDateTime timestamp) {
    this.ticker = ticker;
    this.price = price;
    this.type = type;
    this.timestamp = timestamp;

    logger.debug("Built: {}", toString());
  }

  public Double getPrice() {
    return price;
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
    builder.append(getPrice());
    builder.append(", Time: ");
    builder.append(getTimestamp());
    builder.append(", Type: ");
    builder.append(getTickType());

    builder.append("]");

    return builder.toString();
  }
}
