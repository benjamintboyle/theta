package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
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
  private Double filled = null;
  private Double remaining = null;
  private Double avgFillPrice = null;
  // private Long permId = null;
  // private Integer parentId = null;
  // private Double lastFillPrice = null;
  // private Integer clientId = null;
  // private String whyHeld = null;

  public IbOrderHandler(ExecutableOrder order, FlowableEmitter<theta.execution.api.OrderStatus> emitter) {
    this.order = order;
    this.emitter = emitter;
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

      if (orderStatus.getState() == theta.execution.domain.OrderState.FILLED
          || orderStatus.getState() == theta.execution.domain.OrderState.CANCELLED) {
        logger.debug("Sending complete for order: {}", orderStatus);
        emitter.onComplete();
      }
    } else {
      emitter.onError(new IllegalArgumentException("Unknown order status: " + currentOrderState.status()));
    }
  }

  private Optional<theta.execution.api.OrderStatus> buildOrderStatus() {

    Optional<theta.execution.api.OrderStatus> optionalOrderStatus = Optional.empty();
    Optional<theta.execution.domain.OrderState> optionalOrderState = Optional.empty();

    switch (currentOrderState.status()) {
      case ApiPending:
      case PreSubmitted:
      case PendingSubmit:
      case PendingCancel:
        optionalOrderState = Optional.of(theta.execution.domain.OrderState.PENDING);
        break;
      case ApiCancelled:
      case Cancelled:
        optionalOrderState = Optional.of(theta.execution.domain.OrderState.CANCELLED);
        break;
      case Submitted:
        optionalOrderState = Optional.of(theta.execution.domain.OrderState.SUBMITTED);
        break;
      case Filled:
        optionalOrderState = Optional.of(theta.execution.domain.OrderState.FILLED);
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

}
