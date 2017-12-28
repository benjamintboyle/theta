package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class ReverseStockOrder extends AbstractStockOrder {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public ReverseStockOrder(Stock stock, ExecutionAction action, ExecutionType executionType) {
    super(stock, action, executionType);
  }

  @Override
  public Boolean validate(Security security) {
    logger.info("Validating Reverse Stock Theta Trade Order: {}", toString());
    final Set<Boolean> isValid = new HashSet<Boolean>();

    isValid.add(isValidSecurityType(security.getSecurityType()));
    isValid.add(isAbsoluteQuantityEqualOrLess(security.getQuantity()));
    isValid.add(isValidAction(security.getQuantity()));

    return !isValid.contains(Boolean.FALSE);
  }

  private Boolean isValidAction(Double quantity) {
    Boolean isValidAction = Boolean.FALSE;

    switch (getExecutionAction()) {
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
        logger.error("Invalid execution action: {}", getExecutionAction());
    }

    return isValidAction;
  }

  private Boolean isAbsoluteQuantityEqualOrLess(Double quantity) {
    return Math.abs(quantity) >= Math.abs(getQuantity());
  }

  private Boolean isValidSecurityType(SecurityType securityType) {
    return getSecurityType().equals(securityType);
  }
}