package theta.execution.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;

import java.util.Optional;

public class ExecutableOrderFactory {
    private static final Logger logger = LoggerFactory.getLogger(ExecutableOrderFactory.class);

    private ExecutableOrderFactory() {

    }

    /**
     * Create Order that will reverse position. Validate order is not obviously invalid.
     *
     * @param stock         Stock position to reverse
     * @param executionType Execution Type of Order
     * @param limitPrice    Limit Price of Order
     * @return Validated Order to reverse Stock position
     */
    public static ExecutableOrder reverseAndValidateStockPositionOrder(Stock stock,
                                                                       ExecutionType executionType, Optional<Double> limitPrice) {
        logger.debug("Reversing Position: {}", stock);

        final ExecutableOrder order =
                ReverseStockOrderFactory.reverse(stock, executionType, limitPrice);

        if (order == null) {
            throw new IllegalArgumentException("Invalid order built for " + stock);
        }

        return order;
    }

}
