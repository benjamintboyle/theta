package brokers.interactive_brokers.tick.domain;

import theta.tick.api.PriceLevel;
import theta.tick.api.PriceLevelDirection;

public class IbPriceLevel implements PriceLevel {

  private final String ticker;
  private final Double price;
  private final PriceLevelDirection tradeIf;

  public IbPriceLevel(String ticker, Double price, PriceLevelDirection tradeIf) {
    this.ticker = ticker;
    this.price = price;
    this.tradeIf = tradeIf;
  }

  @Override
  public String getTicker() {
    return ticker;
  }

  @Override
  public Double getStrikePrice() {
    return price;
  }

  @Override
  public PriceLevelDirection tradeIf() {
    return tradeIf;
  }

}
