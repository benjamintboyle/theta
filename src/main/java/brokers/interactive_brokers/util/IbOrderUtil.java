package brokers.interactive_brokers.util;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import theta.execution.api.ExecutableOrder;

public class IbOrderUtil {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private IbOrderUtil() {}

  public static Order buildIbOrder(ExecutableOrder order) {
    final Order ibOrder = new Order();

    Optional<Integer> optionalBrokerId = order.getBrokerId();

    if (optionalBrokerId.isPresent()) {
      ibOrder.orderId(optionalBrokerId.get());
    } else {
      ibOrder.orderId(0);
    }

    ibOrder.totalQuantity(order.getQuantity());

    switch (order.getExecutionAction()) {
      case SELL:
        ibOrder.action(Action.SELL);
        break;
      case BUY:
        ibOrder.action(Action.BUY);
        break;
      default:
        logger.error("Expected BUY or SELL for ExecutionAction: {}", order);
    }


    switch (order.getExecutionType()) {
      case MARKET:
        ibOrder.orderType(OrderType.MKT);
        break;
      case LIMIT:
        ibOrder.orderType(OrderType.LMT);

        Optional<Double> optionalLimitPrice = order.getLimitPrice();
        if (optionalLimitPrice.isPresent()) {
          ibOrder.lmtPrice(optionalLimitPrice.get());
        } else {
          logger.warn("Execution Type is LIMIT, but no Limit Price is set.");
        }

        break;
      case STOP:
        ibOrder.orderType(OrderType.STP);
        break;
      case STOP_LIMIT:
        ibOrder.orderType(OrderType.STP_LMT);
        break;
      case TRAILING_STOP:
        ibOrder.orderType(OrderType.TRAIL);
        break;
      default:
        logger.error("Expected MARKET, LIMIT, STOP, STOP_LIMIT, or TRAILING_STOP for ExecutionType: {}", order);
    }

    Supplier<String> lazyToString = () -> IbStringUtil.toStringOrder(ibOrder);

    logger.debug("Built Interactive Brokers Order: {}", lazyToString);

    return ibOrder;
  }
}
