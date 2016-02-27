package brokers.interactive_brokers.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.NewOrderState;
import com.ib.controller.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;

public class IbOrderHandler implements IOrderHandler {
	final Logger logger = LoggerFactory.getLogger(IbOrderHandler.class);

	@Override
	public void orderState(NewOrderState orderState) {
		// ApiDemo.INSTANCE.controller().removeOrderHandler(this);

		logger.info("Order State: {}", orderState);
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
}
