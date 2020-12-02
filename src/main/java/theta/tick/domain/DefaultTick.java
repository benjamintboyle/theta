package theta.tick.domain;

import theta.domain.Ticker;
import theta.tick.api.Tick;

import java.time.Instant;
import java.util.Objects;

public class DefaultTick implements Tick {

    private final Ticker ticker;
    private final TickType type;
    private final double lastPrice;
    private final double bidPrice;
    private final double askPrice;
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
    public DefaultTick(Ticker ticker, TickType type, double lastPrice,
                       double bidPrice, double askPrice, Instant timestamp) {
        this.ticker = ticker;
        this.type = type;
        this.lastPrice = lastPrice;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.timestamp = timestamp;
    }

    @Override
    public double getLastPrice() {
        return lastPrice;
    }

    @Override
    public double getBidPrice() {
        return bidPrice;
    }

    @Override
    public double getAskPrice() {
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
        return Double.compare(that.lastPrice, lastPrice) == 0 &&
                Double.compare(that.bidPrice, bidPrice) == 0 &&
                Double.compare(that.askPrice, askPrice) == 0 &&
                ticker.equals(that.ticker) &&
                type == that.type &&
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
