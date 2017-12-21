package brokers.interactive_brokers.execution;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import io.reactivex.FlowableEmitter;
import theta.api.ExecutionHandler;
import theta.execution.api.ExecutableOrder;

public class IbExecutionHandler implements ExecutionHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;

  private final Map<UUID, Integer> thetaToIbIdMap = new HashMap<>();

  public IbExecutionHandler(IbController ibController) {
    logger.info("Starting Interactive Brokers Execution Handler");
    this.ibController = ibController;
  }

  @Override
  public Flowable<String> executeStockEquityMarketOrder(ExecutableOrder order) {
    return Flowable.create(emitter -> {

      final IOrderHandler orderHandler = getOrderHandlerCallback(emitter, order);

      executeOrder(order, orderHandler);

    }, BackpressureStrategy.BUFFER);
  }

  private IOrderHandler getOrderHandlerCallback(FlowableEmitter<String> emitter,
      ExecutableOrder order) {
    return new IOrderHandler() {

      OrderState currentOrderState = null;
      OrderStatus currentOrderStatus = null;
      Double filled = null;
      Double remaining = null;
      Double avgFillPrice = null;
      Long permId = null;
      Integer parentId = null;
      Double lastFillPrice = null;
      Integer clientId = null;
      String whyHeld = null;

      @Override
      public void orderState(OrderState orderState) {
        currentOrderState = orderState;

        sendNext("OrderState");
      }

      @Override
      public void orderStatus(OrderStatus status, double filled, double remaining,
          double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId,
          String whyHeld) {

        currentOrderStatus = status;
        this.filled = filled;
        this.remaining = remaining;
        this.avgFillPrice = avgFillPrice;
        this.permId = permId;
        this.parentId = parentId;
        this.lastFillPrice = lastFillPrice;
        this.clientId = clientId;
        this.whyHeld = whyHeld;

        sendNext("OrderStatus");

        if (status == OrderStatus.Filled && remaining == 0) {
          emitter.onComplete();
          thetaToIbIdMap.remove(order.getId());
        }
      }

      @Override
      public void handle(int errorCode, final String errorMsg) {
        emitter.onNext("Message for Order Id: " + thetaToIbIdMap.get(order.getId()) + ", Ticker: "
            + order.getTicker() + " - Error Code: " + errorCode + ", Error Msg: " + errorMsg);
      }

      private void sendNext(String trigger) {
        final StringBuilder builder = new StringBuilder();

        builder.append("Trigger: ");
        builder.append(trigger);

        builder.append(", Order Number: ");
        builder.append(thetaToIbIdMap.get(order.getId()));

        builder.append(", Ticker: ");
        builder.append(order.getTicker());

        // Order Status
        builder.append(", Order Status: ");
        builder.append(IbStringUtil.toStringOrderStatus(currentOrderStatus, filled, remaining,
            avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));

        // Order State
        builder.append(", Order State: ");
        builder.append(IbStringUtil.toStringOrderState(currentOrderState));

        emitter.onNext(builder.toString());
      }
    };
  }

  private Boolean executeOrder(ExecutableOrder order, IOrderHandler ibOrderHandler) {
    logger.info("Executing order: {}", order.toString());
    final Order ibOrder = IbOrderUtil.buildMarketOrder(order.getQuantity());

    final Contract ibContract = IbOrderUtil.buildStockOrderContract(order.getTicker());

    ibController.getController().placeOrModifyOrder(ibContract, ibOrder, ibOrderHandler);

    thetaToIbIdMap.put(order.getId(), Integer.valueOf(ibOrder.orderId()));

    logger.info("Order " + thetaToIbIdMap.get(order.getId()) + " sent to Broker Servers.");

    return Boolean.TRUE;
  }
}
