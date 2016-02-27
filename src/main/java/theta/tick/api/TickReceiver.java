package theta.tick.api;

import theta.tick.domain.Tick;

public interface TickReceiver {
	public Boolean notifyTick(Tick tick);
}
