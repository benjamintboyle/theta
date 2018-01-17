package theta.tick.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tick {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Double price;
  private final String ticker;
  private final ZonedDateTime timestamp;
  private final TickType type;

  public Tick(final String ticker, final Double price, final TickType type, final ZonedDateTime timestamp) {
    this.ticker = ticker;
    this.price = price;
    this.type = type;
    this.timestamp = timestamp;
    Tick.logger.info("Built Tick: {}", toString());
  }

  public Double getPrice() {
    return price;
  }

  public String getTicker() {
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
    builder.append(", Type: ");
    builder.append(getTickType());
    builder.append(", Time: ");
    builder.append(getTimestamp());

    builder.append("]");

    return builder.toString();
  }
}
