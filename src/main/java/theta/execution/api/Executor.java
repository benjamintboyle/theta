package theta.execution.api;

import theta.domain.ThetaTrade;

public interface Executor {
	public void reverseTrade(ThetaTrade trade);
}
