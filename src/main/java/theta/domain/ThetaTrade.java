package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.tick.api.PriceLevel;
import theta.tick.api.PriceLevelDirection;

public class ThetaTrade implements PriceLevel {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id = UUID.randomUUID();
  private final Stock stock;
  private final ShortStraddle straddle;

  private ThetaTrade(Stock stock, ShortStraddle straddle) {
    this.stock = stock;
    this.straddle = straddle;
    logger.info("Built Theta: {}", this);
  }

  public static Optional<ThetaTrade> of(Stock stock, Option call, Option put) {

    final Optional<ShortStraddle> straddle = ShortStraddle.of(call, put);

    Optional<ThetaTrade> theta = Optional.empty();

    if (straddle.isPresent()) {
      theta = ThetaTrade.of(stock, straddle.get());
    }

    return theta;
  }

  public static Optional<ThetaTrade> of(Stock stock, ShortStraddle straddle) {

    Optional<ThetaTrade> theta = Optional.empty();

    if (ThetaTrade.isValidCoveredStraddle(stock, straddle)) {
      theta = Optional.of(new ThetaTrade(stock, straddle));
    }

    return theta;
  }

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
  public Integer getQuantity() {
    return Integer.valueOf(getStock().getQuantity().intValue() / 100);
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
  public Double getStrikePrice() {
    return getCall().getStrikePrice();
  }

  @Override
  public String getTicker() {
    return getStock().getTicker();
  }

  @Override
  public PriceLevelDirection tradeIf() {
    PriceLevelDirection priceLevelDirection = PriceLevelDirection.FALLS_BELOW;

    if (getStock().getQuantity() > 0) {
      priceLevelDirection = PriceLevelDirection.FALLS_BELOW;
    }
    if (getStock().getQuantity() < 0) {
      priceLevelDirection = PriceLevelDirection.RISES_ABOVE;
    }

    return priceLevelDirection;
  }

  @Override
  public String toString() {
    return "ThetaTrade [id=" + getId() + ", type=" + getSecurityType() + ", quantity=" + getQuantity() + ", stock="
        + getStock() + ", call=" + getCall() + ", put=" + getPut() + "]";
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

    if (obj instanceof ThetaTrade) {
      final ThetaTrade other = (ThetaTrade) obj;

      return Objects.equals(getStock(), other.getStock()) && Objects.equals(getStraddle(), other.getStraddle());
    }

    return false;
  }

  private static boolean isValidCoveredStraddle(Stock stock, ShortStraddle straddle) {

    boolean isValid = false;

    // All same ticker
    if (stock.getTicker().equals(straddle.getTicker())) {
      // If stock quantities are multiple of 100 to options
      if (Math.abs(stock.getQuantity().intValue() / 100) == Math.abs(straddle.getQuantity().intValue())) {
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
