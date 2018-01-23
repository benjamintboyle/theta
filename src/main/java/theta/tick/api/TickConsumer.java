package theta.tick.api;

import theta.domain.Ticker;

public interface TickConsumer {
  public void acceptTick(Ticker ticker);
}
