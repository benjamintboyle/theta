package brokers.interactive_brokers.execution.order;

import brokers.interactive_brokers.domain.DefaultIbOrderStatus;
import brokers.interactive_brokers.domain.IbOrderStatus;
import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.DefaultOrderStatus;

import java.util.Objects;

public class DefaultIbOrderHandler implements IbOrderHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultIbOrderHandler.class);

    private final ExecutableOrder order;

    private final ReplayProcessor<theta.execution.api.OrderStatus> orderStatusProcessor =
            ReplayProcessor.create();

    private OrderStatus ibOrderStatus = OrderStatus.ApiPending;
    private double commission = 0.0;
    private double filled = 0.0;
    private double remaining = 0.0;
    private double avgFillPrice = 0.0;

    public DefaultIbOrderHandler(ExecutableOrder order) {
        this.order = Objects.requireNonNull(order, "Order cannot be null");
    }

    @Override
    public Flux<theta.execution.api.OrderStatus> getOrderStatus() {
        return orderStatusProcessor;
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

        orderStatusProcessor.onNext(orderStatus);

        if (orderStatus.getState().equals(theta.execution.api.OrderState.FILLED)
                || orderStatus.getState().equals(theta.execution.api.OrderState.CANCELLED)) {
            logger.debug("Sending complete for order: {}", orderStatus);
            orderStatusProcessor.onComplete();
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
            case ApiPending, PreSubmitted, PendingSubmit, PendingCancel -> orderState = theta.execution.api.OrderState.PENDING;
            case ApiCancelled, Cancelled -> orderState = theta.execution.api.OrderState.CANCELLED;
            case Submitted -> orderState = theta.execution.api.OrderState.SUBMITTED;
            case Filled -> orderState = theta.execution.api.OrderState.FILLED;
            default -> {
                logger.warn("Unknown order status from brokerage: {}. Setting Order State to PENDING.",
                        ibOrderStatus);
                orderState = theta.execution.api.OrderState.PENDING;
            }
        }

        return new DefaultOrderStatus(order, orderState, commission, Math.round(filled),
                Math.round(remaining), avgFillPrice);
    }

    @Override
    public String toString() {
        return "Order: " + order +
                ", Order Status: " + ibOrderStatus +
                ", Commission: " + commission +
                ", Filled: " + filled +
                ", Remaining: " + remaining +
                ", Average Price: " + avgFillPrice;
    }
}
