package brokers.interactive_brokers.util;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;

public class IbOrderUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Order buildMarketOrder(Double quantity) {
    final Order ibOrder = new Order();

    if (quantity > 0) {
      ibOrder.action(Action.SELL);
    } else {
      ibOrder.action(Action.BUY);
    }
    ibOrder.totalQuantity(2 * Math.abs(quantity));
    ibOrder.orderType(OrderType.MKT);
    ibOrder.orderId(0);

    logger.info("Built Interactive Brokers Order: {}", IbStringUtil.toStringOrder(ibOrder));

    return ibOrder;
  }
}
