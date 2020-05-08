package theta.api;

import java.util.Set;
import reactor.core.publisher.Flux;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;

public interface TickHandler {

  public Flux<Tick> getTicks();

  public Ticker getTicker();

  public int addPriceLevelMonitor(PriceLevel priceLevel);

  public int removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored();

  public void cancel();
}
