package theta.api;

import java.util.Set;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;

public interface TickHandler {

  public Tick getLatestTick();

  public Integer addPriceLevelMonitor(PriceLevel priceLevel);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored();

}
