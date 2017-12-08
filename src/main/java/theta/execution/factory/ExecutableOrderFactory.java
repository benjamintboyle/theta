package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.ThetaTrade;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.ExecutionAction;
import theta.execution.domain.ExecutionType;
import theta.execution.domain.ReverseStockThetaTradeOrder;

public class ExecutableOrderFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Optional<ExecutableOrder> reverseStockPosition(ThetaTrade trade) {
    logger.info("Reversing Theta Trade: {}", trade);

    ExecutionAction action = ExecutionAction.BUY;
    if (trade.getEquity().getQuantity() > 0) {
      action = ExecutionAction.SELL;
    }

    ExecutableOrder order =
        new ReverseStockThetaTradeOrder(trade.getEquity(), action, ExecutionType.MARKET);

    logger.info("Validating trade of Security: {}, using Order: {}", trade.getEquity(),
        order.toString());
    if (!order.validate(trade.getEquity())) {
      logger.error("Invalid order for Reverse Trade of Security: {}, using Order: {}",
          trade.getEquity(), order.toString());
      order = null;
    }

    return Optional.ofNullable(order);
  }
}
