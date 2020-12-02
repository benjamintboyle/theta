package theta.domain.composed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.UUID;

public class ShortStraddle implements Security {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
     * @param put  The Put option to use for building Straddle.
     * @return The ShortStraddle based on Call and Put passed in.
     */
    public static ShortStraddle of(Option call, Option put) {
        final ShortStraddle straddle = new ShortStraddle(call, put);

        straddle.validate();

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

    public Option getCall() {
        return call;
    }

    public Option getPut() {
        return put;
    }


    /**
     * Validate that Short Straddle inputs are two short options, one call, one put, with equal strike
     * price and expiration date.
     */
    private void validate() {
        final String prefixMessage = "Short Straddle - ";

        // Are equal tickers
        if (!getCall().getTicker().equals(put.getTicker())) {
            logger.error("{}Tickers are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
            throw new IllegalArgumentException(prefixMessage + "Tickers don't match. Call: " + getCall().getTicker() + ", Put: " + getPut().getTicker());
        }

        // Is CALL
        if (getCall().getSecurityType() != SecurityType.CALL) {
            logger.error("{}Option is not a CALL: {}", prefixMessage, getCall());
            throw new IllegalArgumentException(prefixMessage + "Call assigned is actually " + getCall().getSecurityType());
        }

        // Is PUT
        if (getPut().getSecurityType() != SecurityType.PUT) {
            logger.error("{}Option is not a PUT: {}", prefixMessage, getPut());
            throw new IllegalArgumentException(prefixMessage + "Put assigned is actually " + getPut().getSecurityType());
        }

        // Quantity must be less than 0 (because it is a SHORT straddle)
        if (getCall().getQuantity() >= 0 || getPut().getQuantity() >= 0) {
            logger.error("{}Quantities are not less than zero - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
            throw new IllegalArgumentException(prefixMessage + "Call and/or Put quantities are not less than zero. [" + getCall().getQuantity() + ", " + getPut().getQuantity() + "]");
        }

        // Are equal quantities
        if (getCall().getQuantity() != (getPut().getQuantity())) {
            logger.error("{}Quantities are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
            throw new IllegalArgumentException(prefixMessage + "Call and Put quantities do not match. [" + getCall().getQuantity() + ", " + getPut().getQuantity() + "]");
        }

        // Strike prices must be equal
        if (Double.compare(getCall().getStrikePrice(), getPut().getStrikePrice()) != 0) {
            logger.error("{}Strike Prices are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
            throw new IllegalArgumentException(prefixMessage + "Call and Put strike prices do not match. [" + getCall().getStrikePrice() + ", " + getPut().getStrikePrice() + "]");
        }

        // Are equal expiration date
        if (!getCall().getExpiration().equals(getPut().getExpiration())) {
            logger.error("{}Expirations are not equal - Call: {}, Put: {}", prefixMessage, getCall(), getPut());
            throw new IllegalArgumentException(prefixMessage + "Call and Put expirations do not match. [" + getCall().getExpiration() + ", " + getPut().getExpiration() + "]");
        }
    }

    @Override
    public String toString() {
        return getSecurityType() + " [" + getCall() + ", " + getPut() + ", Id: " + getId() + "]";
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

        if (obj instanceof ShortStraddle other) {
            return Objects.equals(getCall(), other.getCall()) && Objects.equals(getPut(), other.getPut());
        }

        return false;
    }
}
