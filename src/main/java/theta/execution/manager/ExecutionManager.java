package theta.execution.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.ExecutionHandler;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionMonitor;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.domain.EquityOrder;

public class ExecutionManager implements Executor, ExecutionMonitor {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private ExecutionHandler executionHandler;

	private List<Executable> activeOrders = new ArrayList<Executable>();

	public ExecutionManager(ExecutionHandler executionHandler) {
		this.logger.info("Starting Execution Manager");
		this.executionHandler = executionHandler;
	}

	@Override
	public void reverseTrade(ThetaTrade trade) {
		logger.info("Reversing Trade: {}", trade.toString());
		ExecutionAction action = null;
		if (trade.getEquity().getQuantity() > 0) {
			action = ExecutionAction.SELL;
		} else {
			action = ExecutionAction.BUY;
		}

		Executable order = new EquityOrder(trade.getTicker(), trade.getEquity().getQuantity(), action,
				ExecutionType.MARKET);
		this.execute(trade.getEquity(), order);
	}

	private void execute(Security security, Executable order) {
		logger.info("Executing trade of Security: {}, using Order: {}", security.toString(), order.toString());
		if (order.validate(security)) {
			if (this.addActiveTrade(order)) {
				this.executionHandler.executeOrder(order);
			}
		}
	}

	private Boolean addActiveTrade(Executable order) {
		logger.info("Adding Active Trade: {} to Execution Monitor", order.toString());
		Boolean isTradeUnique = Boolean.FALSE;

		List<Executable> matchingActiveTrades = this.activeOrders.stream()
				.filter(active -> active.getTicker().equals(order.getTicker()))
				.filter(active -> active.getExecutionAction().equals(order.getExecutionAction()))
				.collect(Collectors.toList());

		if (matchingActiveTrades.isEmpty()) {
			this.activeOrders.add(order);
			isTradeUnique = Boolean.TRUE;
		} else {
			logger.error("Attempting to repeat order: {}", order);
		}

		return isTradeUnique;
	}

	@Override
	public Boolean portfolioChange(Security security) {
		logger.info("Execution Monitor was notified that Portfolio changed: {}", security.toString());
		Boolean activeTradeRemoved = Boolean.FALSE;

		for (Iterator<Executable> i = this.activeOrders.iterator(); i.hasNext();) {
			Executable active = i.next();

			if (active.getTicker().equals(security.getTicker())) {
				if (active.getExecutionAction().equals(ExecutionAction.BUY) && security.getQuantity() > 0) {
					if (active.getQuantity().equals(security.getQuantity())) {
						logger.info("Removing Security: {} from Execution Monitor", active.toString());
						i.remove();
					} else {
						logger.error("Active Order: {} doesn't match portfolio update: {}", active, security);
					}
				} else if (active.getExecutionAction().equals(ExecutionAction.SELL) && security.getQuantity() < 0) {
					if (active.getQuantity().equals(security.getQuantity())) {
						logger.info("Removing Security: {} from Execution Monitor", active.toString());
						i.remove();
					} else {
						logger.error("Active Order: {} doesn't match portfolio update: {}", active, security);
					}
				}
			}
		}

		return activeTradeRemoved;
	}
}
