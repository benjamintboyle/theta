package brokers.interactive_brokers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;

public class IbOrderUtil {
	private static final Logger logger = LoggerFactory.getLogger(IbOrderUtil.class);

	public static NewContract buildStockOrderContract(String ticker) {
		NewContract contract = new NewContract(new Contract());
		contract.symbol(ticker);
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");

		logger.info("Built Interactive Brokers New Contract: {}",
				IbStringUtil.contractToString(contract.getContract()));

		return contract;
	}

	public static NewOrder buildMarketOrder(Integer quantity) {
		NewOrder ibOrder = new NewOrder();

		if (quantity > 0) {
			ibOrder.action(Action.SELL);
		} else {
			ibOrder.action(Action.BUY);
		}
		ibOrder.totalQuantity(2 * Math.abs(quantity));
		ibOrder.orderType(OrderType.MKT);
		ibOrder.orderId(0);

		logger.info("Built Interactive Brokers Order: {}", IbStringUtil.newOrderToString(ibOrder));

		return ibOrder;
	}
}
