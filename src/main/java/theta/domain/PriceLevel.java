package theta.domain;

public interface PriceLevel extends Comparable<PriceLevel> {
    Ticker getTicker();

    double getPrice();

    PriceLevelDirection tradeIf();
}
