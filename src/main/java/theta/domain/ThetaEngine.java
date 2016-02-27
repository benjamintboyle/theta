package theta.domain;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.connection.manager.ConnectionManager;
import theta.execution.api.Executor;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.api.PortfolioReceiver;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.api.TickReceiver;
import theta.tick.manager.TickManager;

public class ThetaEngine {

	public final static Logger logger = LoggerFactory.getLogger(ThetaEngine.class);

	// Managers
	private ConnectionManager connectionManager;
	private ExecutionManager executionManager;
	private TickManager monitor;
	private PortfolioManager portfolioManager;

	public ThetaEngine(ConnectionManager connectionManager, PortfolioManager portfolioManager, TickManager tickManager,
			ExecutionManager executionManager) {
		this.connectionManager = connectionManager;
		this.portfolioManager = portfolioManager;
		this.monitor = tickManager;
		this.executionManager = executionManager;
	}

	public PortfolioReceiver getPortfolioReceiver() {
		return this.portfolioManager;
	}

	public TickReceiver getTickReceiver() {
		return this.monitor;
	}

	public Executor getExecutor() {
		return this.executionManager;
	}

	public ArrayList<String> getAccountList() {
		return this.connectionManager.getAccountList();
	}

	public void addMonitor(ThetaTrade trade) {
		this.monitor.addMonitor(trade);
	}

	public void shutdown() {
		// Signal main loop to end
		logger.info("Shutting down system");
	}

	public void reverseTrade(ThetaTrade trade) {
		this.executionManager.reverseTrade(trade);
	}
}
