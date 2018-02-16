package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class ShortStraddle implements Security {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Option call;
  private final Option put;

  private final UUID id = UUID.randomUUID();

  private ShortStraddle(Option call, Option put) {
    this.call = call;
    this.put = put;
  }

  public static ShortStraddle of(Option call, Option put) {

    ShortStraddle straddle = new ShortStraddle(call, put);

    if (!straddle.isValid()) {
      logger.error("Short Straddle invalid: {}", straddle);
    }

    logger.debug("Built {}", straddle);

    return straddle;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public SecurityType getSecurityType() {
    return SecurityType.SHORT_STRADDLE;
  }

  @Override
  public Ticker getTicker() {
    return call.getTicker();
  }

  @Override
  public long getQuantity() {
    return call.getQuantity();
  }

  @Override
  public double getPrice() {
    return getStrikePrice();
  }

  public Double getStrikePrice() {
    return call.getStrikePrice();
  }

  public LocalDate getExpiration() {
    return call.getExpiration();
  }

  public Option getCall() {
    return call;
  }

  public Option getPut() {
    return put;
  }


  /**
   * Validate that Short Straddle inputs are two short options, one call, one put, with equal strike
   * price and expiration date.
   *
   * @param call
   * @param put
   * @return
   */
  private boolean isValid() {

    boolean isValidShortStraddle = true;
    final String prefixMessage = "Short Straddle - ";

    // Are equal tickers
    if (!getCall().getTicker().equals(put.getTicker())) {
      logger.error("{}Tickers are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    // Is CALL
    if (getCall().getSecurityType() != SecurityType.CALL) {
      logger.error("{}Option is not a CALL: {}", prefixMessage, getCall());
      isValidShortStraddle = false;
    }

    // Is PUT
    if (getPut().getSecurityType() != SecurityType.PUT) {
      logger.error("{}Option is not a PUT: {}", prefixMessage, getPut());
      isValidShortStraddle = false;
    }

    // Are equal quantities
    if (!(getCall().getQuantity() == (getPut().getQuantity()))) {
      logger.error("{}Quantities are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    // Quantity must be less than 0 (becasue it is a SHORT straddle)
    if (getCall().getQuantity() >= 0 || getPut().getQuantity() >= 0) {
      logger.error("{}Quantities are not less than zero - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    // Are equal strike price
    if (Double.compare(getCall().getStrikePrice(), getPut().getStrikePrice()) == 0) {
      logger.error("{}Strike Prices are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    // Are equal expiration price
    if (!getCall().getExpiration().equals(getPut().getExpiration())) {
      logger.error("{}Expirations are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    return isValidShortStraddle;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(getSecurityType());
    builder.append(" [ ");

    builder.append(getCall());
    builder.append(", ");
    builder.append(getPut());

    builder.append(", Id: ");
    builder.append(getId());

    builder.append("]");

    return builder.toString();
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId(), getCall(), getPut());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof ShortStraddle) {

      ShortStraddle other = (ShortStraddle) obj;

      return Objects.equals(getId(), other.getId()) && Objects.equals(getCall(), other.getCall())
          && Objects.equals(getPut(), other.getPut());
    }

    return false;
  }

}
