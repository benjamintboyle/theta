package theta.tick.api;

import theta.api.ManagerShutdown;
import theta.domain.composed.Theta;

public interface TickMonitor extends ManagerShutdown {

  void addMonitor(Theta theta);

  Integer deleteMonitor(Theta theta);
}
