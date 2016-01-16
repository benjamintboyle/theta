package theta.execution.api;

import theta.strategies.api.Security;

public interface Executable {
	public Boolean validate(Security security);
}
