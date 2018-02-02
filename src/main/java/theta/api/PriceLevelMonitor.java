package theta.api;

import java.util.List;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.TickConsumer;

public interface PriceLevelMonitor {
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickConsumer tickConsumer);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public List<PriceLevel> getPriceLevelsMonitored(Ticker ticker);
}
