package theta.managers;

import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

import brokers.interactive_brokers.IbOrderHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.NewOrder;

import theta.ThetaEngine;
import theta.strategies.ThetaTrade;

public class ExecutionManager {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private ThetaEngine callback;

	// private IbOrderHandler orderHandler = new IbOrderHandler();

	public ExecutionManager(ThetaEngine callback) {
		this.logger.info("Starting subsystem: 'Execution Manager'");
		this.callback = callback;
	}

	public void reverseTrade(ThetaTrade trade) {
		NewOrder order = new NewOrder();

		if (trade.getEquity().getQuantity() > 0) {
			order.action(Action.SELL);
		} else {
			order.action(Action.BUY);
		}
		order.totalQuantity(2 * Math.abs(trade.getEquity().getQuantity()));
		order.orderType(OrderType.MKT);
		// If orderId = 0, then controller assigns Id
		order.orderId(0);

		if (this.isValidTrade(trade, order)) {
			this.callback.controller().placeOrModifyOrder(trade.getEquity().getContract(), order, new IbOrderHandler());
		}
	}

	// TODO Structure of this method needs serious improvement
	private Boolean isValidTrade(ThetaTrade trade, NewOrder order) {
		Boolean isTradeValid = Boolean.FALSE;

		// TODO Check that amount to trade is equal to equity/call/put positions
		if (order.totalQuantity() == trade.getEquity().getQuantity()) {
			this.logger.info("Order quantity [{}] equal to Equity owned [{}]", order.totalQuantity(),
					trade.getEquity().getQuantity());
			if (order.totalQuantity() == trade.getCall().getQuantity() * 100) {
				this.logger.info("Order quantity [{}] equal to Call options owned [{}]", order.totalQuantity(),
						trade.getCall().getQuantity());
				if (order.totalQuantity() == trade.getPut().getQuantity() * 100) {
					this.logger.info("Order quantity [{}] equal to Put options owned [{}]", order.totalQuantity(),
							trade.getPut().getQuantity());
					// Confirm correct BUY/SELL
					if ((order.orderType().equals(Action.BUY) && trade.getEquity().getQuantity() < 0)
							|| (order.orderType().equals(Action.SELL) && trade.getEquity().getQuantity() > 0)) {
						this.logger.info("{} order valid as current Equity position is opposite", order.orderType());
						// TODO Check if existing orders; may not belong in
						// validation method
						// if (this.checkActiveOrders()) {
						// isTradeValid = Boolean.TRUE;
						// }
						isTradeValid = Boolean.TRUE;
					} else {
						this.logger.error("{} order invalid as current Equity position is same", order.orderType());
					}
				} else {
					this.logger.error("Order quantity [{}] not equal to Put options owned [{}]", order.totalQuantity(),
							trade.getPut().getQuantity());
				}
			} else {
				this.logger.error("Order quantity [{}] not equal to Call options owned [{}]", order.totalQuantity(),
						trade.getCall().getQuantity());
			}
		} else {
			this.logger.error("Order quantity [{}] not equal to Equity owned [{}]", order.totalQuantity(),
					trade.getEquity().getQuantity());
		}

		return isTradeValid;
	}
}
