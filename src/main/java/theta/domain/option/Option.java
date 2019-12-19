package theta.domain.option;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;

@Slf4j
public class Option implements Security {

  private final UUID id;
  private final SecurityType type;
  private final Ticker ticker;
  private final long quantity;
  private final double strikePrice;
  private final LocalDate expiration;
  private final double averageTradePrice;

  public Option(SecurityType type, Ticker ticker, long quantity, double strikePrice,
      LocalDate expiration, double averageTradePrice) {
    this(UUID.randomUUID(), type, ticker, quantity, strikePrice, expiration, averageTradePrice);
  }

  /**
   * Build a native Option from parameters.
   *
   * @param id ID of security.
   * @param type SecurityType (Call, Put, etc) of Option.
   * @param ticker Ticker of Option.
   * @param quantity How many Options.
   * @param strikePrice Strike Price of Option(s).
   * @param expiration Time Option(s) expire.
   * @param averageTradePrice Average cost of Option(s).
   */
  public Option(UUID id, SecurityType type, Ticker ticker, long quantity, double strikePrice,
      LocalDate expiration, double averageTradePrice) {

    this.id = Objects.requireNonNull(id, "Id must not be null");
    this.type = Objects.requireNonNull(type, "Security Type must not be null");
    this.ticker = Objects.requireNonNull(ticker, "Ticker must not be null");
    this.quantity = quantity;
    this.strikePrice = strikePrice;
    this.expiration = Objects.requireNonNull(expiration, "Expiration Date must not be null");
    this.averageTradePrice = averageTradePrice;

    log.debug("Built {}", this);
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

    final StringBuilder builder = new StringBuilder();

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
    return Objects.hash(getSecurityType(), getTicker(), getQuantity(), getStrikePrice(),
        getExpiration(), getAverageTradePrice());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof Option) {

      final Option other = (Option) obj;

      return Objects.equals(getSecurityType(), other.getSecurityType())
          && Objects.equals(getTicker(), other.getTicker())
          && Objects.equals(getQuantity(), other.getQuantity())
          && Objects.equals(getStrikePrice(), other.getStrikePrice())
          && Objects.equals(getExpiration(), other.getExpiration())
          && Objects.equals(getAverageTradePrice(), other.getAverageTradePrice());
    }

    return false;
  }
}
