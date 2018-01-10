package theta.api;

import theta.tick.api.TickConsumer;

public interface TickSubscriber {
  public TickHandler subscribeTick(String ticker, TickConsumer tickObserver);

  public void unsubscribeTick(TickHandler tickHandler);
}
