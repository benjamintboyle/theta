package theta.execution.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.ExecutionHandler;
import theta.domain.ThetaTrade;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.domain.EquityOrder;

public class ExecutionManager implements Executor {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private ExecutionHandler executionHandler;

	public ExecutionManager(ExecutionHandler executionHandler) {
		this.logger.info("Starting subsystem: 'Execution Manager'");
		this.executionHandler = executionHandler;
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
			this.executionHandler.executeOrder(order);
		}
	}
}
