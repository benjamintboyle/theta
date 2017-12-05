package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOrderUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;

public class IbExecutionHandler implements ExecutionHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = ibController;
  }

  // TODO: Replaces executeOrder
  @Override
  public Flowable<String> executeStockEquityMarketOrder(ExecutableOrder order) {
    return Flowable.create(emitter -> {

      final IOrderHandler orderHandler = new IOrderHandler() {
        @Override
        public void orderState(OrderState orderState) {
          emitter.onNext("Order State: " + IbStringUtil.toStringOrderState(orderState));
        }

        @Override
        public void orderStatus(OrderStatus status, double filled, double remaining,
            double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId,
            String whyHeld) {
          emitter.onNext("Order Status: " + IbStringUtil.toStringOrderStatus(status, filled,
              remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));

          if (status == OrderStatus.Filled) {
            emitter.onComplete();
          }
        }

        @Override
        public void handle(int errorCode, final String errorMsg) {
          emitter.onNext("Error Code: " + errorCode + ", Error Msg: " + errorMsg);
        }
      };

      executeOrder(order, orderHandler);

    }, BackpressureStrategy.BUFFER);
  }

  private Boolean executeOrder(ExecutableOrder order, IOrderHandler orderHandler) {
    logger.info("Executing order: {}", order.toString());
    final Order ibOrder = IbOrderUtil.buildMarketOrder(order.getQuantity());

    final Contract contract = IbOrderUtil.buildStockOrderContract(order.getTicker());

    logger.info("Sending Order to Broker Servers...");
    ibController.getController().placeOrModifyOrder(contract, ibOrder, orderHandler);

    return Boolean.TRUE;
  }
}
