package theta.api;

import java.util.Optional;
import java.util.Set;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;
import theta.tick.api.TickConsumer;
import theta.tick.processor.TickProcessor;

public interface TickSubscriber {

  public Optional<Tick> getLastestTick(Ticker ticker);

  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickConsumer tickConsumer, TickProcessor tickProcessor);

  public Integer removePriceLevelMonitor(PriceLevel priceLevel);

  public Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker);

}
