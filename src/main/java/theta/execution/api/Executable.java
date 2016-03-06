package theta.execution.api;

import theta.api.Security;

public interface Executable {
	public String getTicker();

	public ExecutionAction getExecutionAction();

	public ExecutionType getExecutionType();

	public Integer getQuantity();

	public Boolean validate(Security security);
}
