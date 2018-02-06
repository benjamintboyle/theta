package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class ReverseStockOrder extends AbstractStockOrder {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ReverseStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType) {
    super(stock, quantity, action, executionType);
  }

  private ReverseStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType,
      Double limitPrice) {
    super(stock, quantity, action, executionType, limitPrice);
  }

  public static ReverseStockOrder reverse(Stock stock, ExecutionType executionType, Optional<Double> limitPrice) {

    ExecutionAction action = ExecutionAction.BUY;
    if (stock.getQuantity() > 0) {
      action = ExecutionAction.SELL;
    }

    long reversedQuantity = 2 * Math.abs(stock.getQuantity());

    ReverseStockOrder executableOrder;

    if (executionType == ExecutionType.LIMIT) {
      executableOrder = new ReverseStockOrder(stock, reversedQuantity, action, executionType, limitPrice.get());
    } else {
      executableOrder = new ReverseStockOrder(stock, reversedQuantity, action, executionType);
    }

    return executableOrder;
  }

  @Override
  public Boolean isValid(Security security) {

    logger.debug("Validating Reverse Stock Theta Trade Order: {}", toString());

    boolean isValid = true;

    if (!isValidSecurityType(security.getSecurityType())) {
      logger.error("Security Types do not match: {} != {}, {}, {}", getSecurityType(), security.getSecurityType(),
          toString(), security);
      isValid = false;
    }


    if (!isAbsoluteQuantityDouble(security.getQuantity())) {
      logger.error("Security Order Quantity is not double: {} != {}, {}, {}", getQuantity(), security.getQuantity(),
          toString(), security);
      isValid = false;
    }

    if (!isValidAction(security.getQuantity())) {
      logger.error("Execution Action is incorrect based on Security quantity: {} != {}, {}, {}", getExecutionAction(),
          security.getQuantity(), toString(), security);
      isValid = false;
    }

    return isValid;
  }

  private Boolean isAbsoluteQuantityDouble(long quantity) {
    return getQuantity() == 2 * Math.abs(quantity);
  }

  private Boolean isValidSecurityType(SecurityType securityType) {
    return getSecurityType().equals(securityType);
  }

  private boolean isValidAction(long quantity) {
    boolean isValidAction = false;

    switch (getExecutionAction()) {
      case BUY:
        if (quantity < 0) {
          isValidAction = true;
        }
        break;
      case SELL:
        if (quantity > 0) {
          isValidAction = true;
        }
        break;
      default:
        isValidAction = false;
        logger.error("Invalid execution action: {}", getExecutionAction());
    }

    return isValidAction;
  }

}
