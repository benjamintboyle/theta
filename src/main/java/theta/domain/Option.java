package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Option implements Security {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private UUID id = UUID.randomUUID();
  private final SecurityType type;
  private final Ticker ticker;
  private final long quantity;
  private final double strikePrice;
  private final LocalDate expiration;
  private final double averageTradePrice;

  public Option(UUID id, SecurityType type, Ticker ticker, long quantity, double strikePrice, LocalDate expiration,
      double averageTradePrice) {

    this.id = Objects.requireNonNull(id, "Id must not be null");
    this.type = Objects.requireNonNull(type, "Security Type must not be null");
    this.ticker = Objects.requireNonNull(ticker, "Ticker must not be null");
    this.quantity = quantity;
    this.strikePrice = strikePrice;
    this.expiration = Objects.requireNonNull(expiration, "Expiration Date must not be null");
    this.averageTradePrice = averageTradePrice;

    Supplier<String> lazyToString = this::toString;

    logger.debug("Built {}", lazyToString);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public SecurityType getSecurityType() {
    return type;
  }

  @Override
  public Ticker getTicker() {
    return ticker;
  }

  @Override
  public long getQuantity() {
    return quantity;
  }

  public double getStrikePrice() {
    return strikePrice;
  }

  @Override
  public double getPrice() {
    return getStrikePrice();
  }

  public LocalDate getExpiration() {
    return expiration;
  }

  public double getAverageTradePrice() {
    return averageTradePrice;
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
    builder.append(", Strike Price: ");
    builder.append(getPrice());
    builder.append(", Expiration: ");
    builder.append(getExpiration());
    builder.append(", Average Price: ");
    builder.append(getAverageTradePrice());
    builder.append(", Id: ");
    builder.append(getId());

    builder.append("]");

    return builder.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSecurityType(), getTicker(), getQuantity(), getStrikePrice(), getExpiration(),
        getAverageTradePrice());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof Option) {

      final Option other = (Option) obj;

      return Objects.equals(getSecurityType(), other.getSecurityType())
          && Objects.equals(getTicker(), other.getTicker()) && Objects.equals(getQuantity(), other.getQuantity())
          && Objects.equals(getStrikePrice(), other.getStrikePrice())
          && Objects.equals(getExpiration(), other.getExpiration())
          && Objects.equals(getAverageTradePrice(), other.getAverageTradePrice());
    }

    return false;
  }
}
