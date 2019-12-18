package theta.api;

import io.reactivex.rxjava3.core.Flowable;
import java.util.Set;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

public interface TickSubscriber {

  public Flowable<Tick> getTicksAcrossStrikePrices();

  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker);

  public void unsubscribeAll();

}
