package theta.tick.api;

import theta.domain.ThetaTrade;

public interface Monitor {

	public void addMonitor(ThetaTrade trade);
}