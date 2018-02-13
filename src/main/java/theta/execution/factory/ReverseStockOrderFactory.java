package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.DefaultStockOrder;
import theta.execution.domain.ExecutionAction;
import theta.execution.domain.ExecutionType;

public class ReverseStockOrderFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static ExecutableOrder reverse(Stock stock, ExecutionType executionType, Optional<Double> limitPrice) {

    ExecutionAction action = ExecutionAction.BUY;
    if (stock.getQuantity() > 0) {
      action = ExecutionAction.SELL;
    }

    long reversedQuantity = 2 * Math.abs(stock.getQuantity());

    DefaultStockOrder executableOrder;

    if (executionType == ExecutionType.LIMIT) {
      executableOrder = new DefaultStockOrder(stock, reversedQuantity, action, executionType, limitPrice.get());
    } else {
      executableOrder = new DefaultStockOrder(stock, reversedQuantity, action, executionType);
    }

    if (!ReverseStockOrderFactory.isValid(stock, executableOrder)) {
      logger.error("Invalid order for Reverse Trade of: {}, for {}", executableOrder, stock);
      executableOrder = null;
    }

    return executableOrder;
  }

  private static boolean isValid(Security security, ExecutableOrder order) {

    logger.debug("Validating Reverse Stock Theta Trade Order: {}", order);

    boolean isValid = true;

    if (!isValidSecurityType(security.getSecurityType(), order)) {
      logger.error("Security Types do not match: {} != {}, {}, {}", order.getSecurityType(), security.getSecurityType(),
          order, security);
      isValid = false;
    }


    if (!isAbsoluteQuantityDouble(security.getQuantity(), order)) {
      logger.error("Security Order Quantity is not double: {} != {}, {}, {}", order.getQuantity(),
          security.getQuantity(), order, security);
      isValid = false;
    }

    if (!isValidAction(security.getQuantity(), order)) {
      logger.error("Execution Action is incorrect based on Security quantity: {} != {}, {}, {}",
          order.getExecutionAction(), security.getQuantity(), order, security);
      isValid = false;
    }

    return isValid;
  }

  private static Boolean isAbsoluteQuantityDouble(long quantity, ExecutableOrder order) {
    return order.getQuantity() == 2 * Math.abs(quantity);
  }

  private static Boolean isValidSecurityType(SecurityType securityType, ExecutableOrder order) {
    return order.getSecurityType().equals(securityType);
  }

  private static boolean isValidAction(long quantity, ExecutableOrder order) {
    boolean isValidAction = false;

    switch (order.getExecutionAction()) {
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
        logger.error("Invalid execution action: {}", order.getExecutionAction());
    }

    return isValidAction;
  }

}
