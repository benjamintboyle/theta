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
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutableOrder order;
  private final Emitter<String> emitter;

  private OrderState currentOrderState = null;
  private OrderStatus currentOrderStatus = null;
  private Double filled = null;
  private Double remaining = null;
  private Double avgFillPrice = null;
  private Long permId = null;
  private Integer parentId = null;
  private Double lastFillPrice = null;
  private Integer clientId = null;
  private String whyHeld = null;

  public IbOrderHandler(ExecutableOrder order, Emitter<String> emitter) {
    this.order = order;
    this.emitter = emitter;
  }

  @Override
  public void orderState(OrderState orderState) {

    logger.debug("Received OrderState: Order Id: {}, Ticker: {}, {}", order.getBrokerId().orElse(null),
        order.getTicker(), IbStringUtil.toStringOrderState(orderState));

    currentOrderState = orderState;

    sendNext("OrderState");
  }

  @Override
  public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
      int parentId, double lastFillPrice, int clientId, String whyHeld) {

    logger.debug("Received OrderStatus: Order Id: {}, Ticker: {}, {}", order.getBrokerId().orElse(null),
        order.getTicker(), IbStringUtil.toStringOrderStatus(status, filled, remaining, avgFillPrice, permId, parentId,
            lastFillPrice, clientId, whyHeld));

    currentOrderStatus = status;
    this.filled = filled;
    this.remaining = remaining;
    this.avgFillPrice = avgFillPrice;
    this.permId = permId;
    this.parentId = parentId;
    this.lastFillPrice = lastFillPrice;
    this.clientId = clientId;
    this.whyHeld = whyHeld;

    sendNext("OrderStatus");

    if (this.currentOrderStatus == OrderStatus.Filled && this.remaining == 0) {
      logger.debug("Sending complete for order: {}", order);
      emitter.onComplete();
    }
  }

  @Override
  public void handle(int errorCode, final String errorMsg) {
    logger.error("Order Handler Error, Error Code: {}, Message: []", errorCode, errorMsg);
    emitter.onError(new Exception("Error from Interactive Brokers for Order: " + order.getBrokerId().orElse(null)));
  }

  private void sendNext(String trigger) {
    final StringBuilder builder = new StringBuilder();

    builder.append("Trigger: ");
    builder.append(trigger);

    builder.append(", Order Id: ");
    builder.append(order.getBrokerId().orElse(null));

    builder.append(", Ticker: ");
    builder.append(order.getTicker());

    // Order Status
    builder.append(", Order Status: ");
    builder.append(IbStringUtil.toStringOrderStatus(currentOrderStatus, filled, remaining, avgFillPrice, permId,
        parentId, lastFillPrice, clientId, whyHeld));

    // Order State
    builder.append(", Order State: ");
    builder.append(IbStringUtil.toStringOrderState(currentOrderState));

    emitter.onNext(builder.toString());
  }

}
