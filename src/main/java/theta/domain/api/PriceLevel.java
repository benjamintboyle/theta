package theta.domain.api;

import theta.domain.Ticker;

public interface PriceLevel extends Comparable<PriceLevel> {
  public Ticker getTicker();

  public Double getStrikePrice();

  public PriceLevelDirection tradeIf();
}
