package theta.execution.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

import brokers.interactive_brokers.handlers.IbOrderHandler;
import theta.connection.api.Controllor;
import theta.domain.ThetaTrade;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.domain.EquityOrder;

public class ExecutionManager implements Executor {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private Controllor controllor;

	public ExecutionManager(Controllor controllor) {
		this.logger.info("Starting subsystem: 'Execution Manager'");
		this.controllor = controllor;
	}

	@Override
	public void reverseTrade(ThetaTrade trade) {
		ExecutionAction action = null;
		if (trade.getEquity().getQuantity() > 0) {
			action = ExecutionAction.SELL;
		} else {
			action = ExecutionAction.BUY;
		}

		Executable order = new EquityOrder(trade.getEquity().getQuantity(), action, ExecutionType.MARKET);
		if (order.validate(trade.getEquity())) {
			this.execute(order);
		}
	}

	public void execute(Executable order) {
		NewOrder ibOrder = new NewOrder();

		if (order.getQuantity() > 0) {
			ibOrder.action(Action.SELL);
		} else {
			ibOrder.action(Action.BUY);
		}
		ibOrder.totalQuantity(2 * Math.abs(order.getQuantity()));
		ibOrder.orderType(OrderType.MKT);
		ibOrder.orderId(0);

		NewContract contract = new NewContract(new Contract());

		this.controllor.controller().placeOrModifyOrder(contract, ibOrder, new IbOrderHandler());
	}
}
