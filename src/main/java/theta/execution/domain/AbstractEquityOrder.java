package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;

public abstract class AbstractEquityOrder implements ExecutableOrder {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id;
  private final SecurityType securityType = SecurityType.STOCK;
  private final String ticker;
  private final Double quantity;
  private final ExecutionAction action;
  private final ExecutionType executionType;

  public AbstractEquityOrder(UUID id, String ticker, Double quantity, ExecutionAction action,
      ExecutionType executionType) {
    this.id = id;
    this.ticker = ticker;
    this.quantity = quantity;
    this.action = action;
    this.executionType = executionType;
    logger.info("Built Equity Order: {}", toString());
  }

  public AbstractEquityOrder(Security security, ExecutionAction action,
      ExecutionType executionType) {
    id = security.getId();
    ticker = security.getTicker();
    quantity = security.getQuantity();
    this.action = action;
    this.executionType = executionType;
    logger.info("Built Equity Order: {}", toString());
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String getTicker() {
    return ticker;
  }

  @Override
  public SecurityType getSecurityType() {
    return securityType;
  }

  @Override
  public Double getQuantity() {
    return quantity;
  }

  @Override
  public ExecutionAction getExecutionAction() {
    return action;
  }

  @Override
  public ExecutionType getExecutionType() {
    return executionType;
  }

  @Override
  public String toString() {
    return "EquityOrder [id=" + getId() + ", securityType=" + getSecurityType() + ", ticker="
        + getTicker() + ", quantity=" + getQuantity() + ", action=" + getExecutionAction()
        + ", executionType=" + getExecutionType() + "]";
  }
}
