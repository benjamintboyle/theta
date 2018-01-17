package theta.execution.api;

import java.util.Optional;
import java.util.UUID;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.domain.ExecutionAction;
import theta.execution.domain.ExecutionType;

public interface ExecutableOrder {

  public UUID getId();

  public Optional<Integer> getBrokerId();

  public void setBrokerId(Integer orderId);

  public String getTicker();

  public SecurityType getSecurityType();

  public ExecutionAction getExecutionAction();

  public ExecutionType getExecutionType();

  public Double getQuantity();

  public Boolean validate(Security security);
}
