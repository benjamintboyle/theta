package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;

public class ExecutableOrderFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ExecutableOrderFactory() {

  }

  /**
   * Create Order that will reverse position. Validate order is not obviously invalid.
   *
   * @param stock Stock position to reverse
   * @param executionType Execution Type of Order
   * @param limitPrice Limit Price of Order
   * @return Validated Order to reverse Stock position
   */
  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Stock stock,
      ExecutionType executionType, Optional<Double> limitPrice) {
    logger.debug("Reversing Position: {}", stock);

    final ExecutableOrder order =
        ReverseStockOrderFactory.reverse(stock, executionType, limitPrice);

    return Optional.ofNullable(order);
  }

}
