package theta.api;

import java.util.List;
import theta.tick.api.PriceLevel;
import theta.tick.api.TickConsumer;

public interface PriceLevelMonitor {
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickConsumer tickConsumer);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public List<PriceLevel> getPriceLevelsMonitored(String ticker);
}
