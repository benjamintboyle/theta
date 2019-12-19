package theta.execution.factory;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;

@Slf4j
public class ExecutableOrderFactory {

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
    log.debug("Reversing Position: {}", stock);

    final ExecutableOrder order =
        ReverseStockOrderFactory.reverse(stock, executionType, limitPrice);

    return Optional.ofNullable(order);
  }

}
