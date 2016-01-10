package brokers.interactive_brokers;

import com.ib.controller.ApiController.ILiveOrderHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderStatus;

public class IbActiveOrdersHandler implements ILiveOrderHandler {

	@Override
	public void openOrder(NewContract contract, NewOrder order, NewOrderState orderState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void orderStatus(int orderId, OrderStatus status, int filled, int remaining, double avgFillPrice,
			long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub

	}
}
