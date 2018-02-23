package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.FlowableEmitter;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.DefaultOrderStatus;

public class IbOrderHandler implements IOrderHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final FlowableEmitter<theta.execution.api.OrderStatus> emitter;

  private final ExecutableOrder order;

  private OrderState currentOrderState = null;
  private double filled = 0.0;
  private double remaining = 0.0;
  private double avgFillPrice = 0.0;
  // private long permId = 0L;
  // private int parentId = 0;
  // private double lastFillPrice = 0.0;
  // private int clientId = 0;
  // private String whyHeld = null;

  private IbOrderHandler(ExecutableOrder order, FlowableEmitter<theta.execution.api.OrderStatus> emitter) {
    this.order = Objects.requireNonNull(order, "Order cannot be null");
    this.emitter = Objects.requireNonNull(emitter, "Emitter for Order Handler must not be null");
  }

  public static IbOrderHandler of(ExecutableOrder order, FlowableEmitter<theta.execution.api.OrderStatus> emitter) {

    IbOrderHandler handler = new IbOrderHandler(order, emitter);

    handler.sendInitialOrderStatus();

    return handler;
  }

  @Override
  public void orderState(OrderState orderState) {

    logger.debug("Received OrderState: Order Id: {}, Ticker: {}, {}", order.getBrokerId().orElse(null),
        order.getTicker(), IbStringUtil.toStringOrderState(orderState));

    currentOrderState = orderState;
  }

  @Override
  public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
      int parentId, double lastFillPrice, int clientId, String whyHeld) {

    logger.debug("Received OrderStatus: Order Id: {}, Ticker: {}, {}", order.getBrokerId().orElse(null),
        order.getTicker(), IbStringUtil.toStringOrderStatus(status, filled, remaining, avgFillPrice, permId, parentId,
            lastFillPrice, clientId, whyHeld));

    currentOrderState.status(status);
    this.filled = filled;
    this.remaining = remaining;
    this.avgFillPrice = avgFillPrice;
    // this.permId = permId;
    // this.parentId = parentId;
    // this.lastFillPrice = lastFillPrice;
    // this.clientId = clientId;
    // this.whyHeld = whyHeld;

    processOrderStatus();
  }

  @Override
  public void handle(int errorCode, final String errorMsg) {
    logger.error("Order Handler Error, Error Code: {}, Message: []", errorCode, errorMsg);
    // TODO: Not all "Errors" received back are fatal (most seem not to be)
    // emitter.onError(new Exception("Error from Interactive Brokers for Order: " +
    // order.getBrokerId().orElse(null)));
  }

  private void processOrderStatus() {

    Optional<theta.execution.api.OrderStatus> optionalOrderStatus = buildOrderStatus();

    if (optionalOrderStatus.isPresent()) {

      theta.execution.api.OrderStatus orderStatus = optionalOrderStatus.get();

      emitter.onNext(orderStatus);

      if (orderStatus.getState() == theta.execution.api.OrderState.FILLED
          || orderStatus.getState() == theta.execution.api.OrderState.CANCELLED) {
        logger.debug("Sending complete for order: {}", orderStatus);
        emitter.onComplete();
      }
    } else {
      emitter.onError(new IllegalArgumentException("Unknown order status: " + currentOrderState.status()));
    }
  }

  private Optional<theta.execution.api.OrderStatus> buildOrderStatus() {

    Optional<theta.execution.api.OrderStatus> optionalOrderStatus = Optional.empty();
    Optional<theta.execution.api.OrderState> optionalOrderState = Optional.empty();

    switch (currentOrderState.status()) {
      case ApiPending:
      case PreSubmitted:
      case PendingSubmit:
      case PendingCancel:
        optionalOrderState = Optional.of(theta.execution.api.OrderState.PENDING);
        break;
      case ApiCancelled:
      case Cancelled:
        optionalOrderState = Optional.of(theta.execution.api.OrderState.CANCELLED);
        break;
      case Submitted:
        optionalOrderState = Optional.of(theta.execution.api.OrderState.SUBMITTED);
        break;
      case Filled:
        optionalOrderState = Optional.of(theta.execution.api.OrderState.FILLED);
        break;
      default:
        logger.warn("Unknown order status from brokerage: {}", currentOrderState.status());
    }

    if (optionalOrderState.isPresent()) {

      optionalOrderStatus = Optional.of(new DefaultOrderStatus(order, optionalOrderState.get(),
          currentOrderState.commission(), Math.round(filled), Math.round(remaining), avgFillPrice));
    }

    return optionalOrderStatus;
  }

  public void sendInitialOrderStatus() {

    logger.debug("Sending initial Order Status");

    if (currentOrderState == null || !(currentOrderState.status() == OrderStatus.Filled)) {
      orderStatus(OrderStatus.ApiPending, filled, remaining, avgFillPrice, 0L, 0, 0.0, 0, null);
    } else {
      logger.warn("Not sending Initial OrderStatus as state already indicates Filled");
    }
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();

    builder.append("Order: ");
    builder.append(order);

    builder.append(", Order State: ");
    builder.append(IbStringUtil.toStringOrderState(currentOrderState));

    builder.append(", Filled: ");
    builder.append(filled);

    builder.append(", Remaining: ");
    builder.append(remaining);

    builder.append(", Average Price: ");
    builder.append(avgFillPrice);

    return builder.toString();
  }

}
