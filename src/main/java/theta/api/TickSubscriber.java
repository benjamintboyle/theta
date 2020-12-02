package theta.api;

import reactor.core.publisher.Flux;
import theta.domain.PriceLevel;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

public interface TickSubscriber {
    Flux<Tick> getTicksAcrossStrikePrices();

    void addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor);

    int removePriceLevelMonitor(PriceLevel priceLevel);

    void unsubscribeAll();
}
