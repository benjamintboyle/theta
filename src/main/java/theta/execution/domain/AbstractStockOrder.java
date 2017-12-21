package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;

public abstract class AbstractStockOrder implements ExecutableOrder {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Stock stock;
  private final ExecutionAction action;
  private final ExecutionType executionType;

  public AbstractStockOrder(Stock stock, ExecutionAction action, ExecutionType executionType) {
    this.stock = stock;
    this.action = action;
    this.executionType = executionType;
    logger.info("Built Equity Order: {}", toString());
  }

  @Override
  public UUID getId() {
    return getStock().getId();
  }

  @Override
  public String getTicker() {
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
    return "EquityOrder [id=" + getId() + ", securityType=" + getSecurityType() + ", ticker=" + getTicker()
        + ", quantity=" + getQuantity() + ", action=" + getExecutionAction() + ", executionType=" + getExecutionType()
        + "]";
  }
}
