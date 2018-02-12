package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.contracts.StkContract;
import com.ib.controller.ApiController.IOrderHandler;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOrderUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;
import theta.execution.domain.DefaultOrderStatus;
import theta.execution.domain.OrderState;

public class IbExecutionHandler implements ExecutionHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  private final ConcurrentMap<ExecutableOrder, IbOrderHandler> orderHandlerMapper = new ConcurrentHashMap<>();

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = ibController;
  }

  @Override
  public Flowable<OrderStatus> executeStockOrder(ExecutableOrder order) {
    return Flowable.<OrderStatus>create(emitter -> {

      OrderStatus initialOrderStatus =
          new DefaultOrderStatus(order, OrderState.PENDING, 0.0, 0, order.getQuantity(), 0.0);

      emitter.onNext(initialOrderStatus);

      orderHandlerMapper.put(order, new IbOrderHandler(order, emitter));
      executeStockOrder(order, orderHandlerMapper.get(order));

    }, BackpressureStrategy.BUFFER).doOnComplete(

        () -> {
          IbOrderHandler orderHandler = orderHandlerMapper.remove(order);
          logger.debug("Removed Order Handler: {}", orderHandler);
        });
  }

  @Override
  public boolean modifyStockOrder(ExecutableOrder order) {

    logger.debug("Modifying order: {}", order);

    boolean isOrderExecuted = false;

    if (order.getBrokerId().isPresent()) {

      Optional<IbOrderHandler> ibOrderHandler = orderHandlerMapper.entrySet()
          .stream()
          .filter(entry -> entry.getKey().getBrokerId().isPresent())
          .filter(entry -> entry.getKey().getBrokerId().get().equals(order.getBrokerId().get()))
          .map(Entry::getValue)
          .findFirst();

      if (ibOrderHandler.isPresent()) {
        executeStockOrder(order, ibOrderHandler.get());
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
  public Flowable<OrderStatus> cancelStockOrder(ExecutableOrder order) {

    ibController.getController().cancelOrder(order.getBrokerId().get());

    return Flowable.empty();
  }

  private void executeStockOrder(ExecutableOrder order, IOrderHandler ibOrderHandler) {

    final Order ibOrder = IbOrderUtil.buildIbOrder(order);

    final Contract ibContract = new StkContract(order.getTicker().toString());

    ibController.getController().placeOrModifyOrder(ibContract, ibOrder, ibOrderHandler);

    order.setBrokerId(ibOrder.orderId());

    if (order.getBrokerId().isPresent()) {
      logger.debug("Order #{} sent to Broker Servers for: {}", order.getBrokerId().get(), order);
    } else {
      logger.warn(
          "Order Id not set for Order. May indicate an internal error. ExecutableOrder: {}, IB Contract: {}, IB Order: {}",
          order, IbStringUtil.toStringContract(ibContract), IbStringUtil.toStringOrder(ibOrder));
    }
  }

}
