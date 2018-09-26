package theta.tick.domain;

import java.time.Instant;
import theta.domain.Ticker;
import theta.tick.api.Tick;

public class DefaultTick implements Tick {

  private final Ticker ticker;
  private final TickType type;
  private final Double lastPrice;
  private final Double bidPrice;
  private final Double askPrice;
  private final Instant timestamp;

  public DefaultTick(final Ticker ticker, final TickType type, final Double lastPrice, final Double bidPrice,
      final Double askPrice, final Instant timestamp) {
    this.ticker = ticker;
    this.type = type;
    this.lastPrice = lastPrice;
    this.bidPrice = bidPrice;
    this.askPrice = askPrice;
    this.timestamp = timestamp;
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
  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append("[ Ticker: ");
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

    builder.append(" ]");

    return builder.toString();
  }
}
