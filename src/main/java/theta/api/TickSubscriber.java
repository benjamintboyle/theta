package theta.api;

import java.util.Optional;
import theta.domain.Ticker;
import theta.tick.api.Tick;

public interface TickSubscriber extends PriceLevelMonitor {

  public Optional<Tick> getLastTick(Ticker ticker);
}
