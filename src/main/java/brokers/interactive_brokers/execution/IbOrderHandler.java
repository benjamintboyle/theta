package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.Emitter;
import theta.execution.api.ExecutableOrder;

public class IbOrderHandler implements IOrderHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutableOrder order;
  private final Emitter<String> emitter;

  private OrderState currentOrderState = null;
  private final OrderStatus currentOrderStatus = null;
  private final Double filled = null;
  private final Double remaining = null;
  private final Double avgFillPrice = null;
  private final Long permId = null;
  private final Integer parentId = null;
  private final Double lastFillPrice = null;
  private final Integer clientId = null;
  private final String whyHeld = null;

  public IbOrderHandler(ExecutableOrder order, Emitter<String> emitter) {
    this.order = order;
    this.emitter = emitter;
  }

  @Override
  public void orderState(OrderState orderState) {
    logger.debug("Received OrderState: {}", IbStringUtil.toStringOrderState(orderState));
    currentOrderState = orderState;

    sendNext("OrderState received: {}" + currentOrderState);
  }

  @Override
  public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice,
      long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {

    logger.debug(
        "Received OrderStatus: Status: {}, Filled: {}, Remaining: {}, Average Fill Price: {}, Perm Id: {}, Parent Id: {}, Last Fill Price: {}, Client Id: {}, Why Held: {}",
        status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId,
        whyHeld);

    sendNext("OrderStatus");
  }

  @Override
  public void handle(int errorCode, final String errorMsg) {
    // emitter.onNext("Message for Order Id: " + thetaToIbIdMap.get(order.getId()) + ", Ticker: " +
    // order.getTicker()
    // + " - Error Code: " + errorCode + ", Error Msg: " + errorMsg);
  }

  private void sendNext(String trigger) {
    final StringBuilder builder = new StringBuilder();

    builder.append("Trigger: ");
    builder.append(trigger);

    builder.append(", Order Number: ");
    builder.append(order.getId());

    builder.append(", Ticker: ");
    builder.append(order.getTicker());

    // Order Status
    builder.append(", Order Status: ");
    builder.append(IbStringUtil.toStringOrderStatus(currentOrderStatus, filled, remaining,
        avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));

    // Order State
    builder.append(", Order State: ");
    builder.append(IbStringUtil.toStringOrderState(currentOrderState));

    emitter.onNext(builder.toString());
  }

}
