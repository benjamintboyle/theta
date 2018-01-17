package theta.api;

import java.util.Optional;
import theta.tick.domain.Tick;

public interface TickSubscriber extends PriceLevelMonitor {

  public Optional<Tick> getLastTick(String ticker);
}
