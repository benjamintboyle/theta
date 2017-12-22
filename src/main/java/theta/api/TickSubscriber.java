package theta.api;

import theta.tick.api.TickObserver;

public interface TickSubscriber {
  public TickHandler subscribeEquity(String ticker, TickObserver tickObserver);

  public Boolean unsubscribeEquity(TickHandler tickHandler);
}
