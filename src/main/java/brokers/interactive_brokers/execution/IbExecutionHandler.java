package brokers.interactive_brokers.execution;

import brokers.interactive_brokers.IbController;
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
            case STOCK:
                orderStatus = executeStockOrder(order);
                break;
            case CALL:
            case PUT:
            case SHORT_STRADDLE:
            case THETA:
                logger.warn("ExecuteOrder not implemented for Security Type: {}."
                        + "Order will not be executed for order: {}", order.getSecurityType(), order);
                break;
            default:
                logger.warn("Unknown Security Type: {}. Order will not be executed for order: {}",
                        order.getSecurityType(), order);
        }

        return orderStatus.doOnComplete(

                () -> {
                    final IbOrderHandler removedOrderHandler =
                            orderHandlerMapper.remove(order.getBrokerId().get());
                    logger.debug("Removed Order Handler: {}", removedOrderHandler);
                });
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

        final Optional<Integer> optionalBrokerId = order.getBrokerId();
        if (optionalBrokerId.isPresent()) {
            ibController.getController().cancelOrder(optionalBrokerId.get().intValue());
        } else {
            logger.warn("Can not cancel stock order with empty broker id for: {}", order);
        }

        return Flux.empty();
    }

    private Flux<OrderStatus> executeStockOrder(ExecutableOrder order) {

        final Order ibOrder = IbOrderUtil.buildIbOrder(order);

        final Contract ibContract = new StkContract(order.getTicker().getSymbol());

        Flux<theta.execution.api.OrderStatus> orderStatus = Flux.create(sink -> {

            IbOrderHandler orderHandler = DefaultIbOrderHandler.of(order, sink);
            orderHandlerMapper.put(Integer.valueOf(ibOrder.orderId()), orderHandler);
            ibController.getController().placeOrModifyOrder(ibContract, ibOrder, orderHandler);

        });

        if (ibOrder.orderId() > 0) {
            order.setBrokerId(Integer.valueOf(ibOrder.orderId()));

            logger.debug("Order #{} sent to Broker Servers for: {}", Integer.valueOf(ibOrder.orderId()),
                    order);

        } else {

            final IllegalStateException noOrderIdException = new IllegalStateException(
                    "Order Id not set for Order. May indicate an internal error. ExecutableOrder: " + order
                            + ", IB Contract: " + IbStringUtil.toStringContract(ibContract) + ", IB Order: "
                            + IbStringUtil.toStringOrder(ibOrder));

            logger.warn("Id not set for Order", noOrderIdException);

            throw noOrderIdException;
        }

        return orderStatus;
    }

}
