package theta.domain;

public interface PriceLevel extends Comparable<PriceLevel> {
  public Ticker getTicker();

  public Double getPrice();

  public PriceLevelDirection tradeIf();
}
