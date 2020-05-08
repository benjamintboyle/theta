package brokers.interactive_brokers.execution;

import brokers.interactive_brokers.domain.DefaultIbOrderStatus;
import brokers.interactive_brokers.domain.IbOrderStatus;
import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.DefaultOrderStatus;

import java.util.Objects;

public class DefaultIbOrderHandler implements IbOrderHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultIbOrderHandler.class);
    private final ExecutableOrder order;
    private final FluxSink<theta.execution.api.OrderStatus> orderStatusSink;

    private OrderStatus ibOrderStatus = OrderStatus.ApiPending;
    private double commission = 0.0;
    private double filled = 0.0;
    private double remaining = 0.0;
    private double avgFillPrice = 0.0;

    private DefaultIbOrderHandler(ExecutableOrder order,
                                  FluxSink<theta.execution.api.OrderStatus> sink) {
        this.order = Objects.requireNonNull(order, "Order cannot be null");
        orderStatusSink = Objects.requireNonNull(sink, "OrderStatus Sink cannot be null");
    }

    /**
     * Returns an Order Handler for Interactive Brokers.
     *
     * @param order Actual determined order
     * @param sink  Sink to add onNext for OrderStatus updates
     * @return IB order handler
     */
    public static DefaultIbOrderHandler of(ExecutableOrder order,
                                           FluxSink<theta.execution.api.OrderStatus> sink) {

        final DefaultIbOrderHandler defaultOrderHandler = new DefaultIbOrderHandler(order, sink);

        defaultOrderHandler.sendInitialOrderStatus();

        return defaultOrderHandler;
    }

    @Override
    public void orderState(OrderState orderState) {

        logger.debug("Received OrderState: Order Id: {}, Ticker: {}, {}", order.getBrokerId().orElse(null),
                order.getTicker(), IbStringUtil.toStringOrderState(orderState));

        ibOrderStatus = orderState.status();
        commission = orderState.commission();
    }

    @Override
    public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice,
                            long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {

        final IbOrderStatus ibOrderStatusBuilder =
                new DefaultIbOrderStatus.DefaultIbOrderStatusBuilder(status).numberFilled(filled)
                        .numberRemaining(remaining).withAverageFillPrice(avgFillPrice).withPermId(permId)
                        .withParentId(parentId).withLastFillPrice(lastFillPrice).withClientId(clientId)
                        .withHeldReason(whyHeld).build();

        logger.debug("Received OrderStatus: Order Id: {}, Ticker: {}, {}",
                order.getBrokerId().orElse(null), order.getTicker(), ibOrderStatusBuilder);

        ibOrderStatus = status;
        this.filled = filled;
        this.remaining = remaining;
        this.avgFillPrice = avgFillPrice;

        final theta.execution.api.OrderStatus orderStatus = buildOrderStatus();

        orderStatusSink.next(orderStatus);

        if (orderStatus.getState() == theta.execution.api.OrderState.FILLED
                || orderStatus.getState() == theta.execution.api.OrderState.CANCELLED) {
            logger.debug("Sending complete for order: {}", orderStatus);
            orderStatusSink.complete();
        }
    }

    @Override
    public void handle(int errorCode, final String errorMsg) {
        logger.error("Order Handler Error, Error Code: {}, Message: {} for Order: {}",
                errorCode, errorMsg, order);
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
            case Inactive:
            case Unknown:
            default:
                logger.warn("Unknown order status from brokerage: {}. Setting Order State to PENDING.",
                        ibOrderStatus);
                orderState = theta.execution.api.OrderState.PENDING;
        }

        return new DefaultOrderStatus(order, orderState, commission, Math.round(filled),
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
        builder.append(order);

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
