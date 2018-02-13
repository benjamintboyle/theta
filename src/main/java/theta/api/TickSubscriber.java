package theta.api;

import java.util.Set;
import io.reactivex.Flowable;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

public interface TickSubscriber {

  public Flowable<Tick> getTicksAcrossStrikePrices();

  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker);

  public void unsubscribeAll();

}
