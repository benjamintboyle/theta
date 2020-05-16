package brokers.interactive_brokers.util;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.execution.api.ExecutableOrder;

public class IbOrderUtil {
    private static final Logger logger = LoggerFactory.getLogger(IbOrderUtil.class);

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
            case STOP -> ibOrder.orderType(OrderType.STP);
            case STOP_LIMIT -> ibOrder.orderType(OrderType.STP_LMT);
            case TRAILING_STOP -> ibOrder.orderType(OrderType.TRAIL);
            default -> logger.error(
                    "Expected MARKET, LIMIT, STOP, STOP_LIMIT, or TRAILING_STOP for ExecutionType: {}",
                    order);
        }

        logger.debug("Built Interactive Brokers Order: {}", IbStringUtil.toStringOrder(ibOrder));

        return ibOrder;
    }
}
