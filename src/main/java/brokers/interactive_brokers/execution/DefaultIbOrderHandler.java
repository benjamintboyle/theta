package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.DefaultOrderStatus;

public class DefaultIbOrderHandler implements IbOrderHandler {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutableOrder order;

  private final Subject<theta.execution.api.OrderStatus> orderStatusSubject = ReplaySubject.create();

  private OrderStatus ibOrderStatus = OrderStatus.ApiPending;
  private double commission = 0.0;
  private double filled = 0.0;
  private double remaining = 0.0;
  private double avgFillPrice = 0.0;

  private DefaultIbOrderHandler(ExecutableOrder order) {
    this.order = Objects.requireNonNull(order, "Order cannot be null");
  }

  public static DefaultIbOrderHandler of(ExecutableOrder order) {

    final DefaultIbOrderHandler defaultOrderHandler = new DefaultIbOrderHandler(order);

    defaultOrderHandler.sendInitialOrderStatus();

    return defaultOrderHandler;
  }

  public Flowable<theta.execution.api.OrderStatus> getOrderStatus() {
    return orderStatusSubject.toFlowable(BackpressureStrategy.LATEST);
  }

  @Override
  public ExecutableOrder getExecutableOrder() {
    return order;
  }

  @Override
  public void orderState(OrderState orderState) {

    logger.debug("Received OrderState: Order Id: {}, Ticker: {}, {}", getExecutableOrder().getBrokerId().orElse(null),
        getExecutableOrder().getTicker(), IbStringUtil.toStringOrderState(orderState));

    ibOrderStatus = orderState.status();
    commission = orderState.commission();
  }

  @Override
  public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
      int parentId, double lastFillPrice, int clientId, String whyHeld) {

    logger.debug("Received OrderStatus: Order Id: {}, Ticker: {}, {}", getExecutableOrder().getBrokerId().orElse(null),
        getExecutableOrder().getTicker(), IbStringUtil.toStringOrderStatus(status, filled, remaining, avgFillPrice,
            permId, parentId, lastFillPrice, clientId, whyHeld));

    ibOrderStatus = status;
    this.filled = filled;
    this.remaining = remaining;
    this.avgFillPrice = avgFillPrice;

    processOrderStatus();
  }

  @Override
  public void handle(int errorCode, final String errorMsg) {
    logger.error("Order Handler Error, Error Code: {}, Message: {} for Order: {}", errorCode, errorMsg,
        getExecutableOrder());
  }

  private void processOrderStatus() {

    final theta.execution.api.OrderStatus orderStatus = buildOrderStatus();

    orderStatusSubject.onNext(orderStatus);

    if (orderStatus.getState() == theta.execution.api.OrderState.FILLED
        || orderStatus.getState() == theta.execution.api.OrderState.CANCELLED) {
      logger.debug("Sending complete for order: {}", orderStatus);
      orderStatusSubject.onComplete();
    }
  }

  private theta.execution.api.OrderStatus buildOrderStatus() {

    theta.execution.api.OrderState orderState;

    switch (ibOrderStatus) {
      case ApiPending:
      case PreSubmitted:
      case PendingSubmit:
      case PendingCancel:
        orderState = theta.execution.api.OrderState.PENDING;
        break;
      case ApiCancelled:
      case Cancelled:
        orderState = theta.execution.api.OrderState.CANCELLED;
        break;
      case Submitted:
        orderState = theta.execution.api.OrderState.SUBMITTED;
        break;
      case Filled:
        orderState = theta.execution.api.OrderState.FILLED;
        break;
      default:
        logger.warn("Unknown order status from brokerage: {}. Setting Order State to PENDING.", ibOrderStatus);
        orderState = theta.execution.api.OrderState.PENDING;
    }

    return new DefaultOrderStatus(getExecutableOrder(), orderState, commission, Math.round(filled),
        Math.round(remaining), avgFillPrice);
  }

  private void sendInitialOrderStatus() {

    logger.debug("Sending initial Order Status");

    if (ibOrderStatus != OrderStatus.Filled) {
      orderStatus(OrderStatus.ApiPending, filled, remaining, avgFillPrice, 0L, 0, 0.0, 0, null);
    } else {
      logger.warn("Not sending Initial OrderStatus as state already indicates Filled");
    }
  }

  @Override
  public String toString() {

    final StringBuilder builder = new StringBuilder();

    builder.append("Order: ");
    builder.append(getExecutableOrder());

    builder.append(", Order Status: ");
    builder.append(ibOrderStatus);

    builder.append(", Commission: ");
    builder.append(commission);

    builder.append(", Filled: ");
    builder.append(filled);

    builder.append(", Remaining: ");
    builder.append(remaining);

    builder.append(", Average Price: ");
    builder.append(avgFillPrice);

    return builder.toString();
  }

}
