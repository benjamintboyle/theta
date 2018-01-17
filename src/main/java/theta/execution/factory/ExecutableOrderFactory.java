package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Theta;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.ExecutionAction;
import theta.execution.domain.ExecutionType;
import theta.execution.domain.ReverseStockOrder;

public class ExecutableOrderFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Stock stock) {
    logger.info("Reversing Stock Position: {}", stock);

    ExecutionAction action = ExecutionAction.BUY;
    if (stock.getQuantity() > 0) {
      action = ExecutionAction.SELL;
    }

    ExecutableOrder order = new ReverseStockOrder(stock, action, ExecutionType.MARKET);

    logger.info("Validating trade of Security: {}, using Order: {}", stock, order);
    if (!order.validate(stock)) {
      logger.error("Invalid order for Reverse Trade of Security: {}, using Order: {}", stock, order.toString());
      order = null;
    }

    return Optional.ofNullable(order);
  }

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Theta trade) {
    logger.info("Reversing Theta Trade: {}", trade);

    return ExecutableOrderFactory.reverseAndValidateStockPositionOrder(trade.getStock());
  }
}
