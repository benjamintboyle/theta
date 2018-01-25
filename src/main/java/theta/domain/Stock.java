package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Stock implements Security {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id;
  private final Ticker ticker;
  private final long quantity;
  private final Double averageTradePrice;

  private final SecurityType type = SecurityType.STOCK;

  private Stock(final UUID id, final Ticker ticker, final long quantity, final Double averageTradePrice) {

    this.id = Objects.requireNonNull(id, "Stock 'id' must not be null.");
    this.ticker = Objects.requireNonNull(ticker, "Stock 'Ticker' must not be null.");
    this.quantity = Objects.requireNonNull(quantity, "Stock 'Quantity' must not be null.");
    this.averageTradePrice = Objects.requireNonNull(averageTradePrice, "Stock 'Average Price' must not be null.");

    logger.debug("Built {}", toString());
  }

  public static Stock of(final UUID id, final Ticker ticker, final long quantity, final Double averageTradePrice) {
    return new Stock(id, ticker, quantity, averageTradePrice);
  }

  public static Stock of(final Ticker ticker, final long quantity, final Double averageTradePrice) {
    return of(UUID.randomUUID(), ticker, quantity, averageTradePrice);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Double getPrice() {
    return averageTradePrice;
  }

  @Override
  public long getQuantity() {
    return quantity;
  }

  @Override
  public SecurityType getSecurityType() {
    return type;
  }

  @Override
  public Ticker getTicker() {
    return ticker;
  }

  public Stock reversePosition() {
    logger.info("Building Reverse of Stock: {}", toString());
    return new Stock(getId(), getTicker(), -1 * getQuantity(), getPrice());
  }

  @Override
  public boolean equals(final Object obj) {

    boolean isEqual = false;

    if (obj == this) {
      isEqual = true;
    }

    if (obj instanceof Stock) {
      final Stock other = (Stock) obj;

      isEqual = Objects.equals(getId(), other.getId()) && Objects.equals(getTicker(), other.getTicker())
          && Objects.equals(getQuantity(), other.getQuantity()) && Objects.equals(getPrice(), other.getPrice())
          && Objects.equals(getSecurityType(), other.getSecurityType());
    }

    return isEqual;
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId(), getTicker(), getQuantity(), getPrice(), getSecurityType());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(getSecurityType());
    builder.append(" [");

    builder.append("Ticker: ");
    builder.append(getTicker());
    builder.append(", Quantity: ");
    builder.append(getQuantity());
    builder.append(", Price: ");
    builder.append(getPrice());
    builder.append(", Id: ");
    builder.append(getId());

    builder.append("]");

    return builder.toString();
  }
}
