package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Theta;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.ReverseStockOrder;

public class ExecutableOrderFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Stock stock) {
    logger.debug("Reversing Position: {}", stock);

    ExecutableOrder order = ReverseStockOrder.reverse(stock);

    if (!order.isValid(stock)) {
      logger.error("Invalid order for Reverse Trade of: {}, for {}", order, stock);
      order = null;
    }

    return Optional.ofNullable(order);
  }

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Theta trade) {
    logger.info("Reversing Theta Trade: {}", trade);

    return ExecutableOrderFactory.reverseAndValidateStockPositionOrder(trade.getStock());
  }
}
