package theta.api;

import theta.tick.api.TickObserver;

public interface TickSubscriber {
  public TickHandler subscribeTick(String ticker, TickObserver tickObserver);

  public Boolean unsubscribeTick(TickHandler tickHandler);
}
