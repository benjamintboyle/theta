package theta.api;

import reactor.core.publisher.Flux;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;

import java.util.Set;

public interface TickHandler {
    Flux<Tick> getTicks();

    Ticker getTicker();

    int addPriceLevelMonitor(PriceLevel priceLevel);

    int removePriceLevelMonitor(PriceLevel priceLevel);

    Set<PriceLevel> getPriceLevelsMonitored();

    void cancel();
}
