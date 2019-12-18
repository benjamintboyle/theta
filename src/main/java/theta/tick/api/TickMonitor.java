package theta.tick.api;

import theta.api.ManagerShutdown;
import theta.domain.PriceLevel;

public interface TickMonitor extends ManagerShutdown {

  void addMonitor(PriceLevel theta);

  Integer deleteMonitor(PriceLevel theta);
}
