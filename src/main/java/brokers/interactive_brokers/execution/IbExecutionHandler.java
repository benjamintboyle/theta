package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
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

public class IbExecutionHandler implements ExecutionHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = ibController;
  }

  @Override
  public Flowable<String> executeMarketStockOrder(ExecutableOrder order) {
    return Flowable.create(emitter -> {

      executeOrder(order, new IbOrderHandler(order, emitter));

    }, BackpressureStrategy.BUFFER);
  }

  private void executeOrder(ExecutableOrder order, IOrderHandler ibOrderHandler) {

    logger.info("Executing order: {}", order);

    final Order ibOrder = IbOrderUtil.buildMarketOrder(order.getQuantity());

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
