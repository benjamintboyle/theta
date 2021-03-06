package theta.domain.composed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;
import theta.domain.stock.Stock;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.UUID;

public class Theta implements Security {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UUID id = UUID.randomUUID();
    private final Stock stock;
    private final ShortStraddle straddle;

    private Theta(Stock stock, ShortStraddle straddle) {
        this.stock = Objects.requireNonNull(stock, "Stock must not be null");
        this.straddle = Objects.requireNonNull(straddle, "Straddle must not be null");

        logger.debug("Built Theta: {}", this);
    }

    public static Theta of(Stock stock, Option call, Option put) {
        return Theta.of(stock, ShortStraddle.of(call, put));
    }

    /**
     * Create Theta from Stock and ShortStraddle, and return Optional.
     *
     * @param stock    Stock associated with Ticker.
     * @param straddle Short Straddle associated with Ticker.
     * @return Optional Theta if able to be created from inputs.
     */
    public static Theta of(Stock stock, ShortStraddle straddle) {
        if (isValidCoveredStraddle(stock, straddle)) {
            return new Theta(stock, straddle);
        } else {
            throw new IllegalArgumentException("Failed to create theta with " + stock + " and " + straddle);
        }
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
     * @return The number of ThetaTrades in this object
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
        return switch (securityType) {
            case STOCK -> getStock();
            case CALL -> getCall();
            case PUT -> getPut();
            default -> {
                IllegalArgumentException illegalArgumentException = new IllegalArgumentException(
                        securityType + " is an invalid security type for this object.");
                logger.error("Unknown Security Type: {}", securityType, illegalArgumentException);
                throw illegalArgumentException;
            }
        };
    }

    @Override
    public Ticker getTicker() {
        return getStock().getTicker();
    }

    @Override
    public String toString() {
        return getSecurityType() +
                " [Ticker: " + getTicker() +
                ", Quantity: " + getQuantity() +
                ", Price: " + getPrice() +
                ", Id: " + getId() +
                ", " + getStock().toString() +
                ", " + getCall().toString() +
                ", " + getPut().toString() + "]";
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

        if (obj instanceof Theta other) {
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
                logger.error("Stock is not 100 times quantity of option quantity: {}, {}", stock.getQuantity(), straddle.getQuantity());
            }
        } else {
            logger.error("Tickers do not match between stock and straddle: {}, {}", stock, straddle);
        }

        return isValid;
    }
}
