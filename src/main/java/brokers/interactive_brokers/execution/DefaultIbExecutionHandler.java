package brokers.interactive_brokers.execution;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.execution.order.DefaultIbOrderHandler;
import brokers.interactive_brokers.execution.order.IbOrderHandler;
import brokers.interactive_brokers.util.IbOrderUtil;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.contracts.StkContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import theta.api.ExecutionHandler;
import theta.domain.SecurityType;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DefaultIbExecutionHandler implements ExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultIbExecutionHandler.class);

    private final IbController ibController;

    private final ConcurrentMap<Integer, IbOrderHandler> orderHandlerMapper =
            new ConcurrentHashMap<>();

    public DefaultIbExecutionHandler(IbController ibController) {
        logger.info("Starting Interactive Brokers Execution Handler");
        this.ibController = Objects.requireNonNull(ibController, "Controller cannot be null");
    }

    @Override
    public Flux<OrderStatus> executeOrder(ExecutableOrder order) {

        Flux<OrderStatus> orderStatus = Flux.empty();

        if (order.getSecurityType() == SecurityType.STOCK) {
            orderStatus.concatWith(executeStockOrder(order));
        } else {
            logger.error("ExecuteOrder not implemented for Security Type: {}. Order will not be executed for order: {}",
                    order.getSecurityType(), order);
        }

        return orderStatus.doOnComplete(
                () -> order.getBrokerId().ifPresent(id ->
                        logger.debug("Removed Order Handler: {}", orderHandlerMapper.remove(id))));
    }

    @Override
    public boolean modifyOrder(ExecutableOrder order) {

        logger.debug("Modifying order: {}", order);

        boolean isOrderModified = false;
        if (order.getBrokerId().isPresent()) {
            executeStockOrder(order);
            isOrderModified = true;
        } else {
            logger.error("Order will not be executed. Attempting to modify an order without a brokerage Id: {}", order);
        }

        return isOrderModified;
    }

    @Override
    public Flux<OrderStatus> cancelOrder(ExecutableOrder order) {

        order.getBrokerId().ifPresentOrElse(
                id -> ibController.getController().cancelOrder(id),
                () -> logger.warn("Can not cancel stock order with empty broker id for: {}", order));

        return Flux.empty();
    }

    private Flux<OrderStatus> executeStockOrder(ExecutableOrder order) {

        Contract ibContract = new StkContract(order.getTicker().getSymbol());
        Order ibOrder = IbOrderUtil.buildIbOrder(order);
        IbOrderHandler orderHandler = new DefaultIbOrderHandler(order);

        ibController.getController().placeOrModifyOrder(ibContract, ibOrder, orderHandler);

        // Store after call to placeOrModifyOrder, as the call updates the ibOrder id
        orderHandlerMapper.put(ibOrder.orderId(), orderHandler);
        order.setBrokerId(ibOrder.orderId());

        return orderHandler.getOrderStatus();
    }
}
