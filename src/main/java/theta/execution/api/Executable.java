package theta.execution.api;

import java.util.UUID;

import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public interface Executable {

	public UUID getId();

	public String getTicker();

	public SecurityType getSecurityType();

	public ExecutionAction getExecutionAction();

	public ExecutionType getExecutionType();

	public Integer getQuantity();

	public Boolean validate(Security security);
}
