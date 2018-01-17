package theta.api;

import theta.tick.api.PriceLevel;
import theta.tick.api.TickConsumer;

public interface PriceLevelMonitor {
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickConsumer tickConsumer);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);
}
