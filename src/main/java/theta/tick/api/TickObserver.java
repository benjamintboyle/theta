package theta.tick.api;

import theta.tick.domain.Tick;

public interface TickObserver {
	public void notifyTick(Tick tick);
}
