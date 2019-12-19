package brokers.interactivebrokers.util;

import static theta.util.LazyEvaluation.lazy;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import theta.execution.api.ExecutableOrder;

@Slf4j
public class IbOrderUtil {

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

    final Optional<Integer> optionalBrokerId = order.getBrokerId();

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
        log.error("Expected BUY or SELL for ExecutionAction: {}", order);
    }


    switch (order.getExecutionType()) {
      case MARKET:
        ibOrder.orderType(OrderType.MKT);
        break;
      case LIMIT:
        ibOrder.orderType(OrderType.LMT);

        final Optional<Double> optionalLimitPrice = order.getLimitPrice();
        if (optionalLimitPrice.isPresent()) {
          ibOrder.lmtPrice(optionalLimitPrice.get());
        } else {
          log.warn("Execution Type is LIMIT, but no Limit Price is set.");
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
        log.error(
            "Expected MARKET, LIMIT, STOP, STOP_LIMIT, or TRAILING_STOP for ExecutionType: {}",
            order);
    }

    log.debug("Built Interactive Brokers Order: {}",
        lazy(() -> IbStringUtil.toStringOrder(ibOrder)));

    return ibOrder;
  }
}
