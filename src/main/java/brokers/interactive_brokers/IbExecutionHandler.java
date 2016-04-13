package brokers.interactive_brokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderStatus;

import brokers.interactive_brokers.util.IbOrderUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.ExecutionHandler;
import theta.execution.api.Executable;

public class IbExecutionHandler implements IOrderHandler, ExecutionHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbExecutionHandler.class);

	private IbController ibController;

	public IbExecutionHandler(IbController ibController) {
		logger.info("Starting Interactive Brokers Execution Handler");
		this.ibController = ibController;
	}

	@Override
	public void orderState(NewOrderState orderState) {
		// ApiDemo.INSTANCE.controller().removeOrderHandler(this);

		logger.info("Order State: {}", IbStringUtil.newOrderStateToString(orderState));
	}

	@Override
	public void orderStatus(OrderStatus status, int filled, int remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld) {
		logger.info(
				"Order Status: {}, Filled: {}, Remaining: {}, Avg Price: {}, Perm Id: {}, Parent Id: {}, Last Fill Price: {}, Client Id: {}, Why Held: {}",
				status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
	}

	@Override
	public void handle(int errorCode, final String errorMsg) {
		logger.error("Error Code: {}, Error Msg: {}", errorCode, errorMsg);
	}

	@Override
	public Boolean executeOrder(Executable order) {
		logger.info("Executing order: {}", order.toString());
		NewOrder ibOrder = IbOrderUtil.buildMarketOrder(order.getQuantity());

		NewContract contract = IbOrderUtil.buildStockOrderContract(order.getTicker());

		logger.info("Sending Order to Broker Servers...");
		this.ibController.getController().placeOrModifyOrder(contract, ibOrder, this);

		return Boolean.TRUE;
	}
}
