package theta.execution.domain;

import java.util.HashSet;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.manager.ExecutionManager;

public class EquityOrder implements Executable {
  private static final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

  private final UUID id;
  private final SecurityType securityType = SecurityType.STOCK;
  private final String ticker;
  private final Double quantity;
  private final ExecutionAction action;
  private final ExecutionType executionType;

  public EquityOrder(UUID id, String ticker, Double quantity, ExecutionAction action,
      ExecutionType executionType) {
    this.id = id;
    this.ticker = ticker;
    this.quantity = quantity;
    this.action = action;
    this.executionType = executionType;
    logger.info("Built Equity Order: {}", toString());
  }

  public EquityOrder(Security security, ExecutionAction action, ExecutionType executionType) {
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
  public Boolean validate(Security security) {
    logger.info("Validating Equity Order: {}", toString());
    final HashSet<Boolean> isValid = new HashSet<Boolean>();

    isValid.add(isValidSecurityType(security.getSecurityType()));
    isValid.add(isAbsoluteQuantityEqualOrLess(security.getQuantity()));
    isValid.add(isValidAction(security.getQuantity()));

    return !isValid.contains(Boolean.FALSE);
  }

  private Boolean isValidAction(Double quantity) {
    Boolean isValidAction = Boolean.FALSE;

    switch (action) {
      case BUY:
        if (quantity < 0) {
          isValidAction = Boolean.TRUE;
        }
        break;
      case SELL:
        if (quantity > 0) {
          isValidAction = Boolean.TRUE;
        }
        break;
      default:
        isValidAction = Boolean.FALSE;
        logger.error("Invalid execution action: {}", action);
    }

    return isValidAction;
  }

  private Boolean isAbsoluteQuantityEqualOrLess(Double quantity) {
    return Math.abs(quantity) >= Math.abs(this.quantity);
  }

  private Boolean isValidSecurityType(SecurityType securityType) {
    return this.securityType.equals(securityType);
  }

  @Override
  public String toString() {
    return "EquityOrder [id=" + id + ", securityType=" + securityType + ", ticker=" + ticker
        + ", quantity=" + quantity + ", action=" + action + ", executionType=" + executionType
        + "]";
  }

}
