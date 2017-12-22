package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;
import brokers.interactive_brokers.util.IbStringUtil;

public class IbOrderHandler implements IOrderHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void orderState(OrderState orderState) {
    // ApiDemo.INSTANCE.controller().removeOrderHandler(this);

    logger.info("Order State: {}", IbStringUtil.toStringOrderState(orderState));
  }

  @Override
  public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
      int parentId, double lastFillPrice, int clientId, String whyHeld) {
    logger.info(
        "Order Status: {}, Filled: {}, Remaining: {}, Avg Price: {}, Perm Id: {}, Parent Id: {}, Last Fill Price: {}, Client Id: {}, Why Held: {}",
        status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
  }

  @Override
  public void handle(int errorCode, final String errorMsg) {
    logger.error("Error Code: {}, Error Msg: {}", errorCode, errorMsg);
  }
}
