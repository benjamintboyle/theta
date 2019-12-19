package theta.domain.composed;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;

@Slf4j
public class ShortStraddle implements Security {

  private final Option call;
  private final Option put;

  private final UUID id = UUID.randomUUID();

  private ShortStraddle(Option call, Option put) {
    this.call = call;
    this.put = put;
  }

  /**
   * Builds a Short Straddle object from a Call and Put Option.
   *
   * @param call The Call option to use for building Straddle.
   * @param put The Put option to use for building Straddle.
   * @return The ShortStraddle based on Call and Put passed in.
   */
  public static ShortStraddle of(Option call, Option put) {

    final ShortStraddle straddle = new ShortStraddle(call, put);

    if (!straddle.isValid()) {
      log.error("Short Straddle invalid: {}", straddle);
    }

    log.debug("Built {}", straddle);

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
    // Quantity of SHORT Straddles should always be positive
    return Math.abs(call.getQuantity());
  }

  @Override
  public double getPrice() {
    return getStrikePrice();
  }

  public double getStrikePrice() {
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
   * @return Is a valid Short Straddle.
   */
  private boolean isValid() {

    boolean isValidShortStraddle = true;
    final String prefixMessage = "Short Straddle - ";

    // Are equal tickers
    if (!getCall().getTicker().equals(put.getTicker())) {
      log.error("{}Tickers are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
      isValidShortStraddle = false;
    }

    // Is CALL
    if (getCall().getSecurityType() != SecurityType.CALL) {
      log.error("{}Option is not a CALL: {}", prefixMessage, getCall());
      isValidShortStraddle = false;
    }

    // Is PUT
    if (getPut().getSecurityType() != SecurityType.PUT) {
      log.error("{}Option is not a PUT: {}", prefixMessage, getPut());
      isValidShortStraddle = false;
    }

    // Are equal quantities
    if (getCall().getQuantity() != (getPut().getQuantity())) {
      log.error("{}Quantities are not equal - Call: {}, Put: {}", prefixMessage, getCall(),
          getPut());
      isValidShortStraddle = false;
    }

    // Quantity must be less than 0 (becasue it is a SHORT straddle)
    if (getCall().getQuantity() >= 0 || getPut().getQuantity() >= 0) {
      log.error("{}Quantities are not less than zero - Call: {}, Put: {}", prefixMessage, getCall(),
          getPut());
      isValidShortStraddle = false;
    }

    // Strike prices must be equal
    if (Double.compare(getCall().getStrikePrice(), getPut().getStrikePrice()) != 0) {
      log.error("{}Strike Prices are not equal - Call: {}, Put: {}", prefixMessage, getCall(),
          getPut());
      isValidShortStraddle = false;
    }

    // Are equal expiration price
    if (!getCall().getExpiration().equals(getPut().getExpiration())) {
      log.error("{}Expirations are not equal - Call: {}, Put: {}", prefixMessage, getCall(),
          getPut());
      isValidShortStraddle = false;
    }

    return isValidShortStraddle;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(getSecurityType());
    builder.append(" [");

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

    return Objects.hash(getCall(), getPut());
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof ShortStraddle) {

      final ShortStraddle other = (ShortStraddle) obj;

      return Objects.equals(getCall(), other.getCall()) && Objects.equals(getPut(), other.getPut());
    }

    return false;
  }

}
