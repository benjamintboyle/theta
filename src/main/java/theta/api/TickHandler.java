package theta.api;

import java.util.Set;
import io.reactivex.Flowable;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;

public interface TickHandler {

  public Flowable<Tick> getTicks();

  public Integer addPriceLevelMonitor(PriceLevel priceLevel);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored();

}
