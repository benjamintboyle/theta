package theta.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.ThetaEngine;
import theta.execution.EquityOrder;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.strategies.ThetaTrade;

public class ExecutionManager {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private ThetaEngine callback;

	public ExecutionManager(ThetaEngine callback) {
		this.logger.info("Starting subsystem: 'Execution Manager'");
		this.callback = callback;
	}

	public void reverseTrade(ThetaTrade trade) {
		ExecutionAction action = null;
		if (trade.getEquity().getQuantity() > 0) {
			action = ExecutionAction.SELL;
		} else {
			action = ExecutionAction.BUY;
		}

		Executable order = new EquityOrder(trade.getEquity().getQuantity(), action, ExecutionType.MARKET);
		if (order.validate(trade.getEquity())) {
			this.callback.execute(trade.getEquity(), order);
		}
	}
}