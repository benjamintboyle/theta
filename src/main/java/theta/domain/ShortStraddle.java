package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.SecurityType;

public class ShortStraddle {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Option call;
  private final Option put;

  private final UUID id = UUID.randomUUID();

  private ShortStraddle(Option call, Option put) {
    this.call = call;
    this.put = put;
  }

  public static Optional<ShortStraddle> of(Option call, Option put) {

    Optional<ShortStraddle> straddle = Optional.empty();

    if (ShortStraddle.isValidStraddle(call, put)) {
      straddle = Optional.of(new ShortStraddle(call, put));
    }

    logger.info("Successfully built straddle: {}", straddle);

    return straddle;
  }

  public UUID getId() {
    return id;
  }

  public SecurityType getSecurityType() {
    return SecurityType.SHORT_STRADDLE;
  }

  public String getTicker() {
    return call.getTicker();
  }

  public Double getQuantity() {
    return call.getQuantity();
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
  private static boolean isValidStraddle(Option call, Option put) {

    boolean isValidShortStraddle = true;
    final String prefixMessage = "Short Straddle - ";

    // Are equal tickers
    if (!call.getTicker().equals(put.getTicker())) {
      logger.error("{}Tickers are not equal - Call: {}, Put: {}", prefixMessage, call, put);
      isValidShortStraddle = false;
    }

    // Is CALL
    if (call.getSecurityType() != SecurityType.CALL) {
      logger.error("{}Option is not a CALL: {}", prefixMessage, call);
      isValidShortStraddle = false;
    }

    // Is PUT
    if (put.getSecurityType() != SecurityType.PUT) {
      logger.error("{}Option is not a PUT: {}", prefixMessage, put);
      isValidShortStraddle = false;
    }

    // Are equal quantities
    if (!call.getQuantity().equals(put.getQuantity())) {
      logger.error("{}Quantities are not equal - Call: {}, Put: {}", prefixMessage, call, put);
      isValidShortStraddle = false;
    }

    // Quantity must be less than 0 (becasue it is a SHORT straddle)
    if (call.getQuantity() >= 0 || put.getQuantity() >= 0) {
      logger.error("{}Quantities are not less than zero - Call: {}, Put: {}", prefixMessage, call, put);
      isValidShortStraddle = false;
    }

    // Are equal strike price
    if (!call.getStrikePrice().equals(put.getStrikePrice())) {
      logger.error("{}Strike Prices are not equal - Call: {}, Put: {}", prefixMessage, call, put);
      isValidShortStraddle = false;
    }

    // Are equal expiration price
    if (!call.getExpiration().equals(put.getExpiration())) {
      logger.error("{}Expirations are not equal - Call: {}, Put: {}", prefixMessage, call, put);
      isValidShortStraddle = false;
    }

    return isValidShortStraddle;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append("Short Straddle ");
    builder.append(getId());
    builder.append(" - Call: ");
    builder.append(getCall());
    builder.append(", Put: ");
    builder.append(getPut());

    return builder.toString();
  }
}
