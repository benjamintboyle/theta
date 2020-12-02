package theta.domain.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Option implements Security {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UUID id;
    private final SecurityType type;
    private final Ticker ticker;
    private final long quantity;
    private final double strikePrice;
    private final LocalDate expiration;
    private final double averageTradePrice;

    /**
     * Build a native Option from parameters.
     *
     * @param id                ID of security.
     * @param type              SecurityType (Call, Put, etc) of Option.
     * @param ticker            Ticker of Option.
     * @param quantity          How many Options.
     * @param strikePrice       Strike Price of Option(s).
     * @param expiration        Time Option(s) expire.
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

        logger.debug("Built {}", this);
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
        return getSecurityType() +
                " [" + "Ticker: " + getTicker() +
                ", Quantity: " + getQuantity() +
                ", Strike Price: " + getPrice() +
                ", Expiration: " + getExpiration() +
                ", Average Price: " + getAverageTradePrice() +
                ", Id: " + getId() + "]";
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

        if (obj instanceof Option other) {

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
