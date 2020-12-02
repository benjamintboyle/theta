package theta.execution.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.domain.CandidateStockOrder;
import theta.execution.domain.DefaultStockOrder;

import java.lang.invoke.MethodHandles;

public class ReverseStockOrderFactory {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReverseStockOrderFactory() {
    }

    public static ExecutableOrder reverse(CandidateStockOrder candidateOrder) {
        DefaultStockOrder executableOrder;

        if (candidateOrder.executionType() == ExecutionType.LIMIT) {
            executableOrder = buildLimitOrder(candidateOrder);
        } else {
            executableOrder = buildMarketOrder(candidateOrder);
        }

        logger.info("{} reversed to {}", candidateOrder, executableOrder);

        return executableOrder;
    }

    private static DefaultStockOrder buildMarketOrder(CandidateStockOrder candidateOrder) {

        return new DefaultStockOrder(
                candidateOrder.stock(),
                reversedQuantity(candidateOrder.stock().getQuantity()),
                calculateExecutionAction(candidateOrder.stock().getQuantity()),
                candidateOrder.executionType());

    }

    private static DefaultStockOrder buildLimitOrder(CandidateStockOrder candidateOrder) {
        if (candidateOrder.limitPrice().isPresent()) {
            return new DefaultStockOrder(
                    candidateOrder.stock(),
                    reversedQuantity(candidateOrder.stock().getQuantity()),
                    calculateExecutionAction(candidateOrder.stock().getQuantity()),
                    candidateOrder.executionType(),
                    candidateOrder.limitPrice().get());
        } else {
            throw new IllegalArgumentException("Limit price not set for " + candidateOrder);
        }
    }

    private static ExecutionAction calculateExecutionAction(long stockQuantity) {
        if (stockQuantity > 0) {
            return ExecutionAction.SELL;
        } else {
            return ExecutionAction.BUY;
        }
    }

    private static long reversedQuantity(long originalQuantity) {
        return 2 * Math.abs(originalQuantity);
    }
}
