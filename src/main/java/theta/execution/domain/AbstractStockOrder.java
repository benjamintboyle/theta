package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Ticker;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;

public abstract class AbstractStockOrder implements ExecutableOrder {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Stock stock;
  private final ExecutionAction action;
  private final ExecutionType executionType;
  private Optional<Integer> brokerId = Optional.empty();

  public AbstractStockOrder(Stock stock, ExecutionAction action, ExecutionType executionType) {
    this.stock = stock;
    this.action = action;
    this.executionType = executionType;
    logger.info("Built Stock Order: {}", toString());
  }

  @Override
  public UUID getId() {
    return getStock().getId();
  }

  @Override
  public Optional<Integer> getBrokerId() {
    return brokerId;
  }

  @Override
  public void setBrokerId(Integer brokerId) {
    this.brokerId = Optional.ofNullable(brokerId);
  }

  @Override
  public Ticker getTicker() {
    return getStock().getTicker();
  }

  @Override
  public SecurityType getSecurityType() {
    return getStock().getSecurityType();
  }

  @Override
  public Double getQuantity() {
    return getStock().getQuantity();
  }

  @Override
  public ExecutionAction getExecutionAction() {
    return action;
  }

  @Override
  public ExecutionType getExecutionType() {
    return executionType;
  }

  private Stock getStock() {
    return stock;
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();

    builder.append("Stock Order [");

    builder.append("Ticker: ");
    builder.append(getTicker());
    builder.append(", Action: ");
    builder.append(getExecutionAction());
    builder.append(", Quantity: ");
    builder.append(getQuantity());
    builder.append(", Security Type: ");
    builder.append(getSecurityType());
    builder.append(", Execution Type: ");
    builder.append(getExecutionType());
    builder.append(", Id: ");
    builder.append(getId());
    builder.append(", Broker Id: ");
    builder.append(getBrokerId().orElse(null));

    builder.append("]");

    return builder.toString();
  }
}
