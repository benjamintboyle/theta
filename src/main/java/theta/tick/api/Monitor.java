package theta.tick.api;

import theta.api.TickHandler;
import theta.domain.ThetaTrade;

public interface Monitor {

	public void addMonitor(ThetaTrade trade);

	public TickHandler deleteMonitor(String ticker);
}
