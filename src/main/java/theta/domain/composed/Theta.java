package theta.domain.composed;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;
import theta.domain.stock.Stock;

@Slf4j
public class Theta implements Security {

  private final UUID id = UUID.randomUUID();
  private final Stock stock;
  private final ShortStraddle straddle;

  private Theta(Stock stock, ShortStraddle straddle) {
    this.stock = Objects.requireNonNull(stock, "Stock must not be null");
    this.straddle = Objects.requireNonNull(straddle, "Straddle must not be null");

    log.debug("Built Theta: {}", this);
  }

  public static Optional<Theta> of(Stock stock, Option call, Option put) {

    return Theta.of(stock, ShortStraddle.of(call, put));
  }

  /**
   * Create Theta from Stock and ShortStraddle, and return Optional.
   *
   * @param stock Stock associated with Ticker.
   * @param straddle Short Straddle associated with Ticker.
   * @return Optional Theta if able to be created from inputs.
   */
  public static Optional<Theta> of(Stock stock, ShortStraddle straddle) {

    Optional<Theta> theta = Optional.empty();

    if (Theta.isValidCoveredStraddle(stock, straddle)) {
      theta = Optional.of(new Theta(stock, straddle));
    }

    return theta;
  }

  @Override
  public UUID getId() {
    return id;
  }

  /**
   * Gets the number of ThetaTrade "contracts". One ThetaTrade "contract" contains a call, a put,
   * and 100 stock. This quantity is negative or positive based on if the stock is long or short.
   * Note, for ThetaTrades the call and put options are both short.
   *
   * @return
   */
  @Override
  public long getQuantity() {
    return Long.signum(getStock().getQuantity()) * Math.abs(getStraddle().getQuantity());
  }

  @Override
  public double getPrice() {
    return getStraddle().getPrice();
  }

  public Stock getStock() {
    return stock;
  }

  public ShortStraddle getStraddle() {
    return straddle;
  }

  public Option getCall() {
    return getStraddle().getCall();
  }

  public Option getPut() {
    return getStraddle().getPut();
  }

  @Override
  public SecurityType getSecurityType() {
    return SecurityType.THETA;
  }

  /**
   * Converts Security Type from Interactive Brokers to native type.
   *
   * @param securityType The Interactive Brokers security type.
   * @return The native Security type.
   */
  public Security getSecurityOfType(SecurityType securityType) {
    Security securityOfType = null;

    switch (securityType) {
      case STOCK:
        securityOfType = getStock();
        break;
      case CALL:
        securityOfType = getCall();
        break;
      case PUT:
        securityOfType = getPut();
        break;
      case SHORT_STRADDLE:
        break;
      case THETA:
      default:
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException(
            securityType + " is an invalid security type for this object.");
        log.error("Unknown Security Type: {}", securityType, illegalArgumentException);
        throw illegalArgumentException;
    }

    return securityOfType;
  }

  @Override
  public Ticker getTicker() {
    return getStock().getTicker();
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

    builder.append(", Price: ");
    builder.append(getPrice());

    builder.append(", Id: ");
    builder.append(getId());

    builder.append(", ");
    builder.append(getStock().toString());

    builder.append(", ");
    builder.append(getCall().toString());

    builder.append(", ");
    builder.append(getPut().toString());

    builder.append("]");

    return builder.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStock(), getStraddle());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof Theta) {
      final Theta other = (Theta) obj;

      return Objects.equals(getStock(), other.getStock())
          && Objects.equals(getStraddle(), other.getStraddle());
    }

    return false;
  }

  private static boolean isValidCoveredStraddle(Stock stock, ShortStraddle straddle) {

    boolean isValid = false;

    // All same ticker
    if (stock.getTicker().equals(straddle.getTicker())) {
      // If stock quantities are multiple of 100 to options
      if (Math.abs(stock.getQuantity()) == Math.abs(straddle.getQuantity() * 100)) {
        isValid = true;
      } else {
        log.error("Stock is not 100 times quantity of option quantity: {}, {}",
            Long.valueOf(stock.getQuantity()), Long.valueOf(straddle.getQuantity()));
      }
    } else {
      log.error("Tickers do not match between stock and straddle: {}, {}", stock, straddle);
    }

    return isValid;
  }

}
