package theta.api;

import java.util.Set;
import io.reactivex.Flowable;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;

public interface TickHandler {

  public Flowable<Tick> getTicks();

  public Ticker getTicker();

  public int addPriceLevelMonitor(PriceLevel priceLevel);

  public int removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored();

  public void cancel();

}
