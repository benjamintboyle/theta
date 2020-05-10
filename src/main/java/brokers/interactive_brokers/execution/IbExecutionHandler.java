package brokers.interactive_brokers.execution;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.execution.order.DefaultIbOrderHandler;
import brokers.interactive_brokers.execution.order.IbOrderHandler;
import brokers.interactive_brokers.util.IbOrderUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.contracts.StkContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class IbExecutionHandler implements ExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(IbExecutionHandler.class);

    private final IbController ibController;

    private final ConcurrentMap<Integer, IbOrderHandler> orderHandlerMapper =
            new ConcurrentHashMap<>();

    public IbExecutionHandler(IbController ibController) {
        logger.info("Starting Interactive Brokers Execution Handler");
        this.ibController = Objects.requireNonNull(ibController, "Controller cannot be null");
    }

    @Override
    public Flux<OrderStatus> executeOrder(ExecutableOrder order) {

        Flux<OrderStatus> orderStatus = Flux.empty();

        switch (order.getSecurityType()) {
            case STOCK -> orderStatus.concatWith(executeStockOrder(order));
            case CALL, PUT, SHORT_STRADDLE, THETA -> logger.warn("ExecuteOrder not implemented for Security Type: {}."
                    + "Order will not be executed for order: {}", order.getSecurityType(), order);
            default -> logger.warn("Unknown Security Type: {}. Order will not be executed for order: {}",
                    order.getSecurityType(), order);
        }

        return orderStatus.doOnComplete(
                () -> order.getBrokerId().ifPresent(id ->
                        logger.debug("Removed Order Handler: {}", orderHandlerMapper.remove(id))));
    }

    @Override
    public boolean modifyOrder(ExecutableOrder order) {

        logger.debug("Modifying order: {}", order);
        boolean isOrderExecuted = false;
        final Optional<Integer> optionalBrokerId = order.getBrokerId();
        if (optionalBrokerId.isPresent()) {
            final IbOrderHandler ibOrderHandler = orderHandlerMapper.get(optionalBrokerId.get());
            if (ibOrderHandler != null) {
                executeStockOrder(order);
                isOrderExecuted = true;
            } else {
                logger.error("Order will not be executed. No Order Handler available for order: {}", order);
            }
        } else {
            logger.error(
                    "Order will not be executed. Attempting to modify an order without a brokerage Id: {}",
                    order);
        }

        return isOrderExecuted;
    }

    @Override
    public Flux<OrderStatus> cancelOrder(ExecutableOrder order) {

        order.getBrokerId().ifPresentOrElse(
                id -> ibController.getController().cancelOrder(id),
                () -> logger.warn("Can not cancel stock order with empty broker id for: {}", order));

        return Flux.empty();
    }

    private Flux<OrderStatus> executeStockOrder(ExecutableOrder order) {

        final Order ibOrder = IbOrderUtil.buildIbOrder(order);
        final int orderId = ibOrder.orderId();

        final Contract ibContract = new StkContract(order.getTicker().getSymbol());

        IbOrderHandler orderHandler = new DefaultIbOrderHandler(order);
        orderHandlerMapper.put(orderId, orderHandler);
        ibController.getController().placeOrModifyOrder(ibContract, ibOrder, orderHandler);

        if (orderId > 0) {
            order.setBrokerId(orderId);
            logger.debug("Order: {} sent to Broker Servers for: {}", orderId, order);
        } else {
            throw illegalStateException(order);
        }

        return orderHandler.getOrderStatus();
    }

    private IllegalStateException illegalStateException(ExecutableOrder order) {
        final IllegalStateException noOrderIdException = new IllegalStateException(
                "Order Id not set for Order. May indicate an internal error. ExecutableOrder: " + order
                        + ", IB Order: " + IbStringUtil.toStringOrder(IbOrderUtil.buildIbOrder(order)));
        logger.warn("Id not set for Order", noOrderIdException);

        return noOrderIdException;
    }
}
