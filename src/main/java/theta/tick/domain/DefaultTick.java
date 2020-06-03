package theta.tick.domain;

import theta.domain.Ticker;
import theta.tick.api.Tick;

import java.time.Instant;
import java.util.Objects;

public class DefaultTick implements Tick {

    private final Ticker ticker;
    private final TickType type;
    private final Double lastPrice;
    private final Double bidPrice;
    private final Double askPrice;
    private final Instant timestamp;

    /**
     * Create Tick via DefaultTick.
     *
     * @param ticker    Ticker symbol for Tick
     * @param type      TickType for Tick
     * @param lastPrice Last Price of Tick
     * @param bidPrice  Bid Price of Tick
     * @param askPrice  Ask Price of Tick
     * @param timestamp Timestamp of Tick
     */
    public DefaultTick(final Ticker ticker, final TickType type, final Double lastPrice,
                       final Double bidPrice, final Double askPrice, final Instant timestamp) {
        this.ticker = ticker;
        this.type = type;
        this.lastPrice = lastPrice;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.timestamp = timestamp;
    }

    @Override
    public Double getLastPrice() {
        return lastPrice;
    }

    @Override
    public Double getBidPrice() {
        return bidPrice;
    }

    @Override
    public Double getAskPrice() {
        return askPrice;
    }

    @Override
    public Ticker getTicker() {
        return ticker;
    }

    @Override
    public TickType getTickType() {
        return type;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTick that = (DefaultTick) o;
        return ticker.equals(that.ticker) &&
                type == that.type &&
                lastPrice.equals(that.lastPrice) &&
                bidPrice.equals(that.bidPrice) &&
                askPrice.equals(that.askPrice) &&
                timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, type, lastPrice, bidPrice, askPrice, timestamp);
    }

    @Override
    public String toString() {
        return "DefaultTick{" +
                "ticker=" + ticker +
                ", type=" + type +
                ", lastPrice=" + lastPrice +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
