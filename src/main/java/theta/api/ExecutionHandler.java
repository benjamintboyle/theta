package theta.api;

import theta.execution.api.Executable;

public interface ExecutionHandler {
	public Boolean executeOrder(Executable order);
}
