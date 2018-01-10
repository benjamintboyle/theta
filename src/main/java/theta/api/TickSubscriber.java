package theta.api;

import java.util.Optional;
import theta.tick.api.TickConsumer;

public interface TickSubscriber {
  public TickHandler subscribeTick(String ticker, TickConsumer tickObserver);

  public void unsubscribeTick(TickHandler tickHandler);

  public Optional<TickHandler> getHandler(String ticker);
}
