package theta.execution.factory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Theta;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;

public class ExecutableOrderFactory {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ExecutableOrderFactory() {}

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Stock stock, ExecutionType executionType,
      Optional<Double> limitPrice) {
    logger.debug("Reversing Position: {}", stock);

    ExecutableOrder order = ReverseStockOrderFactory.reverse(stock, executionType, limitPrice);

    return Optional.ofNullable(order);
  }

  public static Optional<ExecutableOrder> reverseAndValidateStockPositionOrder(Theta trade,
      ExecutionType executionType) {
    logger.info("Reversing Theta Trade: {}", trade);

    return ExecutableOrderFactory.reverseAndValidateStockPositionOrder(trade.getStock(), executionType,
        Optional.of(trade.getCall().getStrikePrice()));
  }

}
