package theta.tick.api;

import theta.api.ManagerController;
import theta.domain.composed.Theta;

public interface TickMonitor extends ManagerController {
    void addMonitor(Theta theta);

    int deleteMonitor(Theta theta);
}
