package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Option implements Security {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private UUID id = UUID.randomUUID();
  private final SecurityType type;
  private final Ticker backingTicker;
  private final long quantity;
  private final Double strikePrice;
  private final LocalDate expiration;
  private final Double averageTradePrice;

  public Option(UUID id, SecurityType type, Ticker backingTicker, long quantity, Double strikePrice,
      LocalDate expiration, Double averageTradePrice) {

    this.id = id;
    this.type = type;
    this.backingTicker = backingTicker;
    this.quantity = quantity;
    this.strikePrice = strikePrice;
    this.expiration = expiration;
    this.averageTradePrice = averageTradePrice;

    logger.debug("Built {}", toString());
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
    return backingTicker;
  }

  @Override
  public long getQuantity() {
    return quantity;
  }

  public Double getStrikePrice() {
    return strikePrice;
  }

  @Override
  public Double getPrice() {
    return getStrikePrice();
  }

  public LocalDate getExpiration() {
    return expiration;
  }

  public Double getAverageTradePrice() {
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
    return Objects.hash(getId(), getSecurityType(), getTicker(), getQuantity(), getStrikePrice(), getExpiration(),
        getAverageTradePrice());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    final Option other = (Option) obj;

    return Objects.equals(getId(), other.getId()) && Objects.equals(getSecurityType(), other.getSecurityType())
        && Objects.equals(getTicker(), other.getTicker()) && Objects.equals(getQuantity(), other.getQuantity())
        && Objects.equals(getStrikePrice(), other.getStrikePrice())
        && Objects.equals(getExpiration(), other.getExpiration())
        && Objects.equals(getAverageTradePrice(), other.getAverageTradePrice());
  }
}
