package theta.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.connection.manager.ConnectionManager;
import theta.domain.ThetaEngine;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.properties.manager.PropertyManager;
import theta.tick.manager.TickManager;

public class Startup {
	public final static Logger logger = LoggerFactory.getLogger(Startup.class);

	// Entry point for application
	public static void main(String[] args) {
		PropertyManager propertyManager = new PropertyManager("config.properties");

		ConnectionManager connectionManager = new ConnectionManager(propertyManager.getProperty("GATEWAY_HOST"),
				Integer.parseInt(propertyManager.getProperty("GATEWAY_PORT")),
				Integer.parseInt(propertyManager.getProperty("CLIENT_ID")));

		ExecutionManager executionManager = new ExecutionManager(connectionManager);

		TickManager tickManager = new TickManager(connectionManager, executionManager);

		PortfolioManager portfolioManager = new PortfolioManager(connectionManager, tickManager);

		ThetaEngine.logger.info("Starting system...");
		new ThetaEngine(portfolioManager, tickManager, executionManager);
	}
}
