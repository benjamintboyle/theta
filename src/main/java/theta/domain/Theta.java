package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Theta implements Security {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id = UUID.randomUUID();
  private final Stock stock;
  private final ShortStraddle straddle;

  private Theta(Stock stock, ShortStraddle straddle) {
    this.stock = stock;
    this.straddle = straddle;
    logger.info("Built Theta: {}", this);
  }

  public static Optional<Theta> of(Stock stock, Option call, Option put) {

    final Optional<ShortStraddle> straddle = ShortStraddle.of(call, put);

    Optional<Theta> theta = Optional.empty();

    if (straddle.isPresent()) {
      theta = Theta.of(stock, straddle.get());
    }

    return theta;
  }

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
   * Gets the number of ThetaTrade "contracts". One ThetaTrade "contract" contains a call, a put, and
   * 100 stock. This quantity is negative or positive based on if the stock is long or short. Note,
   * for ThetaTrades the call and put options are both short.
   *
   * @return
   */
  @Override
  public long getQuantity() {
    return Long.signum(getStock().getQuantity()) * Math.abs(getStraddle().getQuantity());
  }

  @Override
  public Double getPrice() {
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
      default:
        final IllegalArgumentException illegalArgumentException =
            new IllegalArgumentException(securityType + " is an invalid security type for this object.");
        logger.error("Unknown Security Type: {}", securityType, illegalArgumentException);
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
    return Objects.hash(getId(), getStock(), getStraddle());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj instanceof Theta) {
      final Theta other = (Theta) obj;

      return Objects.equals(getStock(), other.getStock()) && Objects.equals(getStraddle(), other.getStraddle());
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
        logger.error("Stock is not 100 times quantity of option quantity: {}, {}", stock.getQuantity(),
            straddle.getQuantity());
      }
    } else {
      logger.error("Tickers do not match between stock and straddle: {}, {}", stock, straddle);
    }

    return isValid;
  }
}
