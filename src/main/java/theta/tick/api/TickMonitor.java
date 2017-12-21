package theta.tick.api;

import theta.domain.ThetaTrade;

public interface TickMonitor {

	public void addMonitor(ThetaTrade theta);

	public Integer deleteMonitor(ThetaTrade theta);
}
