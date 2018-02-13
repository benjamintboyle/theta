package theta.execution.api;

import java.util.Optional;
import java.util.UUID;
import theta.domain.Ticker;
import theta.domain.api.SecurityType;

public interface ExecutableOrder {

  public UUID getId();

  public Ticker getTicker();

  public SecurityType getSecurityType();

  public ExecutionAction getExecutionAction();

  public ExecutionType getExecutionType();

  public Optional<Double> getLimitPrice();

  public long getQuantity();

  public Optional<Integer> getBrokerId();

  public void setBrokerId(Integer orderId);
}
