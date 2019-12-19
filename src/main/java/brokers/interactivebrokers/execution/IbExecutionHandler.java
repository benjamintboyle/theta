package brokers.interactivebrokers.execution;

import brokers.interactivebrokers.IbController;
import brokers.interactivebrokers.util.IbOrderUtil;
import brokers.interactivebrokers.util.IbStringUtil;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.contracts.StkContract;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

@Slf4j
@Component
public class IbExecutionHandler implements ExecutionHandler {

  private final IbController ibController;

  private final ConcurrentMap<Integer, IbOrderHandler> orderHandlerMapper =
      new ConcurrentHashMap<>();

  @Autowired
  public IbExecutionHandler(IbController ibController) {
    log.info("Starting Interactive Brokers Execution Handler");
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
        log.warn("ExecuteOrder not implemented for Security Type: {}."
            + "Order will not be executed for order: {}", order.getSecurityType(), order);
        break;
      default:
        log.warn("Unknown Security Type: {}. Order will not be executed for order: {}",
            order.getSecurityType(), order);
    }

    return orderStatus.doOnComplete(

        () -> {
          final IbOrderHandler removedOrderHandler =
              orderHandlerMapper.remove(order.getBrokerId().get());
          log.debug("Removed Order Handler: {}", removedOrderHandler);
        });
  }

  @Override
  public boolean modifyOrder(ExecutableOrder order) {

    log.debug("Modifying order: {}", order);

    boolean isOrderExecuted = false;

    final Optional<Integer> optionalBrokerId = order.getBrokerId();
    if (optionalBrokerId.isPresent()) {

      final IbOrderHandler ibOrderHandler = orderHandlerMapper.get(optionalBrokerId.get());

      if (ibOrderHandler != null) {

        executeStockOrder(ibOrderHandler);

        isOrderExecuted = true;

      } else {
        log.error("Order will not be executed. No Order Handler available for order: {}", order);
      }

    } else {
      log.error(
          "Order will not be executed. Attempting to modify an order without a brokerage Id: {}",
          order);
    }

    return isOrderExecuted;
  }

  @Override
  public Flowable<OrderStatus> cancelOrder(ExecutableOrder order) {

    final Optional<Integer> optionalBrokerId = order.getBrokerId();
    if (optionalBrokerId.isPresent()) {
      ibController.getController().cancelOrder(optionalBrokerId.get().intValue());
    } else {
      log.warn("Can not cancel stock order with empty broker id for: {}", order);
    }

    return Flowable.empty();
  }

  private void executeStockOrder(IbOrderHandler ibOrderHandler) {

    final Order ibOrder = IbOrderUtil.buildIbOrder(ibOrderHandler.getExecutableOrder());

    final Contract ibContract =
        new StkContract(ibOrderHandler.getExecutableOrder().getTicker().getSymbol());

    ibController.getController().placeOrModifyOrder(ibContract, ibOrder, ibOrderHandler);

    if (ibOrder.orderId() > 0) {
      ibOrderHandler.getExecutableOrder().setBrokerId(Integer.valueOf(ibOrder.orderId()));

      log.debug("Order #{} sent to Broker Servers for: {}", Integer.valueOf(ibOrder.orderId()),
          ibOrderHandler.getExecutableOrder());

      orderHandlerMapper.put(Integer.valueOf(ibOrder.orderId()), ibOrderHandler);

    } else {

      final IllegalStateException noOrderIdException = new IllegalStateException(
          "Order Id not set for Order. May indicate an internal error. ExecutableOrder: "
              + ibOrderHandler.getExecutableOrder() + ", IB Contract: "
              + IbStringUtil.toStringContract(ibContract) + ", IB Order: "
              + IbStringUtil.toStringOrder(ibOrder));

      log.warn("Id not set for Order", noOrderIdException);

      throw noOrderIdException;
    }
  }

}
