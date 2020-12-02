package theta.domain.pricelevel;

import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.Ticker;
import theta.domain.composed.Theta;

import java.util.Objects;

public class DefaultPriceLevel implements PriceLevel {

    private final Ticker ticker;
    private final double price;
    private final PriceLevelDirection tradeIf;

    private DefaultPriceLevel(Ticker ticker, Double price, PriceLevelDirection tradeIf) {
        this.ticker = Objects.requireNonNull(ticker, "DefaultPriceLevel expects non-null ticker: " + ticker);
        this.price = Objects.requireNonNull(price, "DefaultPriceLevel expects non-null price: " + price);
        this.tradeIf = Objects.requireNonNull(tradeIf, "DefaultPriceLevel expects non-null price level direction:" + tradeIf);
    }

    public static PriceLevel of(Theta theta) {
        return new DefaultPriceLevel(theta.getTicker(), theta.getPrice(), calculateTradeIf(theta));
    }

    public static PriceLevel from(Ticker ticker, Double price, PriceLevelDirection tradeIf) {
        return new DefaultPriceLevel(ticker, price, tradeIf);
    }

    @Override
    public Ticker getTicker() {
        return ticker;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public PriceLevelDirection tradeIf() {
        return tradeIf;
    }

    @Override
    public int compareTo(PriceLevel other) {

        int compareValue;

        if (!getTicker().equals(other.getTicker())) {
            compareValue = getTicker().compareTo(other.getTicker());
        } else if (getPrice() != other.getPrice()) {
            compareValue = Double.compare(getPrice(), other.getPrice());
        } else {
            compareValue = tradeIf().toString().compareTo(other.tradeIf().toString());
        }

        return compareValue;
    }

    @Override
    public String toString() {
        return "DefaultPriceLevel{" +
                "ticker=" + ticker +
                ", price=" + price +
                ", tradeIf=" + tradeIf +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultPriceLevel that = (DefaultPriceLevel) o;
        return Double.compare(that.price, price) == 0 &&
                ticker.equals(that.ticker) &&
                tradeIf == that.tradeIf;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, price, tradeIf);
    }

    private static PriceLevelDirection calculateTradeIf(Theta theta) {
        PriceLevelDirection priceLevelDirection = PriceLevelDirection.FALLS_BELOW;

        if (theta.getStock().getQuantity() < 0) {
            priceLevelDirection = PriceLevelDirection.RISES_ABOVE;
        }

        return priceLevelDirection;
    }
}
