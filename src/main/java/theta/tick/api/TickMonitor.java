package theta.tick.api;

import theta.domain.api.PriceLevel;

public interface TickMonitor {

  public void addMonitor(PriceLevel theta);

  public Integer deleteMonitor(PriceLevel theta);
}
