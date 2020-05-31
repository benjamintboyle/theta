package brokers.interactive_brokers.util;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.execution.api.ExecutableOrder;

import java.lang.invoke.MethodHandles;

public class IbOrderUtil {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private IbOrderUtil() {
    }

    /**
     * Builds an Interactive Brokers version of an Executable Order.
     *
     * @param order An ExecutableOrder.
     * @return An Interactive Broker order.
     */
    public static Order buildIbOrder(ExecutableOrder order) {
        final Order ibOrder = new Order();

        order.getBrokerId().ifPresentOrElse(
                ibOrder::orderId,
                () -> ibOrder.orderId(0));

        ibOrder.totalQuantity(order.getQuantity());

        switch (order.getExecutionAction()) {
            case SELL -> ibOrder.action(Action.SELL);
            case BUY -> ibOrder.action(Action.BUY);
            default -> logger.error("Expected BUY or SELL for ExecutionAction: {}", order);
        }

        switch (order.getExecutionType()) {
            case MARKET -> ibOrder.orderType(OrderType.MKT);
            case LIMIT -> {
                ibOrder.orderType(OrderType.LMT);
                order.getLimitPrice().ifPresentOrElse(ibOrder::lmtPrice,
                        () -> logger.warn("Execution Type is LIMIT, but no Limit Price is set for Order: {}", order));
            }
            default -> logger.error("Expected MARKET or LIMIT for ExecutionType: {}", order);
        }

        logger.debug("Built Interactive Brokers Order: {}", IbStringUtil.toStringOrder(ibOrder));

        return ibOrder;
    }
}
