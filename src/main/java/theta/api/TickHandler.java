package theta.api;

import java.util.Set;
import io.reactivex.Flowable;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;

public interface TickHandler {

  public Flowable<Tick> getTicks();

  public Ticker getTicker();

  public Integer addPriceLevelMonitor(PriceLevel priceLevel);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored();

  public void cancel();

}
