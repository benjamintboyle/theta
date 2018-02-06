package theta.execution.api;

import java.util.Optional;
import java.util.UUID;
import theta.domain.Ticker;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.domain.ExecutionAction;
import theta.execution.domain.ExecutionType;

public interface ExecutableOrder {

  public UUID getId();

  public Optional<Integer> getBrokerId();

  public void setBrokerId(Integer orderId);

  public Ticker getTicker();

  public SecurityType getSecurityType();

  public ExecutionAction getExecutionAction();

  public ExecutionType getExecutionType();

  public Optional<Double> getLimitPrice();

  public long getQuantity();

  public Boolean isValid(Security security);
}
