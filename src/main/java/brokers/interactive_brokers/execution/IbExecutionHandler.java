package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.contracts.StkContract;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOrderUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.Flowable;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

public class IbExecutionHandler implements ExecutionHandler {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  private final ConcurrentMap<Integer, IbOrderHandler> orderHandlerMapper = new ConcurrentHashMap<>();

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = Objects.requireNonNull(ibController, "Controller cannot be null");
  }

  @Override
  public Flowable<OrderStatus> executeOrder(ExecutableOrder order) {

    Flowable<OrderStatus> orderStatus = Flowable.empty();

    switch (order.getSecurityType()) {
      case STOCK:
        final DefaultIbOrderHandler orderHandler = DefaultIbOrderHandler.of(order);
        executeStockOrder(orderHandler);
        orderStatus = orderHandler.getOrderStatus();
        break;
      case CALL:
      case PUT:
      case SHORT_STRADDLE:
      case THETA:
        logger.warn("ExecuteOrder not implemented for Security Type: {}. Order will not be executed for order: {}",
            order.getSecurityType(), order);
        break;
      default:
        logger.warn("Unknown Security Type: {}. Order will not be executed for order: {}", order.getSecurityType(),
            order);
    }

    return orderStatus.doOnComplete(

        () -> {
          final IbOrderHandler removedOrderHandler = orderHandlerMapper.remove(order.getBrokerId().get());
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

        executeStockOrder(ibOrderHandler);

        isOrderExecuted = true;

      } else {
        logger.error("Order will not be executed. No Order Handler available for order: {}", order);
      }

    } else {
      logger.error("Order will not be executed. Attempting to modify an order without a brokerage Id: {}", order);
    }

    return isOrderExecuted;
  }

  @Override
  public Flowable<OrderStatus> cancelOrder(ExecutableOrder order) {

    final Optional<Integer> optionalBrokerId = order.getBrokerId();
    if (optionalBrokerId.isPresent()) {
      ibController.getController().cancelOrder(optionalBrokerId.get());
    } else {
      logger.warn("Can not cancel stock order with empty broker id for: {}", order);
    }

    return Flowable.empty();
  }

  private void executeStockOrder(IbOrderHandler ibOrderHandler) {

    final Order ibOrder = IbOrderUtil.buildIbOrder(ibOrderHandler.getExecutableOrder());

    final Contract ibContract = new StkContract(ibOrderHandler.getExecutableOrder().getTicker().getSymbol());

    ibController.getController().placeOrModifyOrder(ibContract, ibOrder, ibOrderHandler);

    if (ibOrder.orderId() > 0) {
      ibOrderHandler.getExecutableOrder().setBrokerId(ibOrder.orderId());

      logger.debug("Order #{} sent to Broker Servers for: {}", ibOrder.orderId(), ibOrderHandler.getExecutableOrder());

      orderHandlerMapper.put(ibOrder.orderId(), ibOrderHandler);

    } else {

      final IllegalStateException noOrderIdException =
          new IllegalStateException("Order Id not set for Order. May indicate an internal error. ExecutableOrder: "
              + ibOrderHandler.getExecutableOrder() + ", IB Contract: " + IbStringUtil.toStringContract(ibContract)
              + ", IB Order: " + IbStringUtil.toStringOrder(ibOrder));

      logger.warn("Id not set for Order", noOrderIdException);

      throw noOrderIdException;
    }
  }

}
