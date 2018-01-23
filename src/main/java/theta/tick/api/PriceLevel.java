package theta.tick.api;

import theta.domain.Ticker;

public interface PriceLevel {
  public Ticker getTicker();

  public Double getStrikePrice();

  public PriceLevelDirection tradeIf();
}
