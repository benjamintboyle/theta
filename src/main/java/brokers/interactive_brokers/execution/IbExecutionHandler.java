package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
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

public class IbExecutionHandler implements ExecutionHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  private final ConcurrentMap<Integer, IbOrderHandler> orderHandlerMapper = new ConcurrentHashMap<>();

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = ibController;
  }

  @Override
  public Flowable<OrderStatus> executeStockOrder(ExecutableOrder order) {
    return Flowable.<OrderStatus>create(emitter -> {

      // TODO: Call to "of" should not be duplicated here and a few lines below in the put
      IbOrderHandler orderHandler = IbOrderHandler.of(order, emitter);

      int orderId = executeStockOrder(order, orderHandler);

      orderHandlerMapper.put(orderId, IbOrderHandler.of(order, emitter));

    }, BackpressureStrategy.LATEST)

        .doOnComplete(

            () -> {
              IbOrderHandler orderHandler = orderHandlerMapper.remove(order.getBrokerId().get());
              logger.debug("Removed Order Handler: {}", orderHandler);
            });
  }

  @Override
  public boolean modifyStockOrder(ExecutableOrder order) {

    logger.debug("Modifying order: {}", order);

    boolean isOrderExecuted = false;

    if (order.getBrokerId().isPresent()) {

      IbOrderHandler ibOrderHandler = orderHandlerMapper.get(order.getBrokerId().get());

      if (ibOrderHandler != null) {

        executeStockOrder(order, ibOrderHandler);

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

  private int executeStockOrder(ExecutableOrder order, IOrderHandler ibOrderHandler) {

    final Order ibOrder = IbOrderUtil.buildIbOrder(order);

    final Contract ibContract = new StkContract(order.getTicker().toString());

    ibController.getController().placeOrModifyOrder(ibContract, ibOrder, ibOrderHandler);

    order.setBrokerId(ibOrder.orderId());

    if (order.getBrokerId().isPresent()) {
      logger.debug("Order #{} sent to Broker Servers for: {}", order.getBrokerId().get(), order);
    }

    return order.getBrokerId().orElseThrow(() -> {
      return new IllegalStateException(
          "Order Id not set for Order. May indicate an internal error. ExecutableOrder: " + order + ", IB Contract: "
              + IbStringUtil.toStringContract(ibContract) + ", IB Order: " + IbStringUtil.toStringOrder(ibOrder));
    });
  }

}
