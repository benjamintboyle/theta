package theta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brokers.interactive_brokers.handlers.IbConnectionHandler;
import brokers.interactive_brokers.handlers.IbController;
import brokers.interactive_brokers.handlers.IbExecutionHandler;
import brokers.interactive_brokers.handlers.IbPositionHandler;
import brokers.interactive_brokers.handlers.IbTickSubscriber;

import theta.api.ConnectionHandler;
import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.TickSubscriber;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

public class ThetaEngine {
	public final static Logger logger = LoggerFactory.getLogger(ThetaEngine.class);

	// Brokerage handlers
	private ConnectionHandler brokerConnectionHandler;
	private ExecutionHandler brokerExecutionHandler;
	private TickSubscriber brokerTickSubscriber;
	private PositionHandler brokerPositionHandler;

	// Theta managers
	private ConnectionManager connectionManager;
	private PortfolioManager portfolioManager;
	private TickManager tickManager;
	private ExecutionManager executionManager;

	public ThetaEngine() {
		// Create Theta Engine
		ThetaEngine thetaEngine = new ThetaEngine();

		// Register shutdown hook
		thetaEngine.attachShutdownHook();

		// Initialize Broker interfaces
		thetaEngine.initializeBrokerageHandlers();

		// Initialize Theta Managers with brokerage interfaces
		thetaEngine.initializeThetaManagers();

		// Register managers with one another as needed
		thetaEngine.registerManagerInterfaces();
	}

	// Entry point for application
	public static void main(String[] args) {
		// Create Theta Engine
		ThetaEngine thetaEngine = new ThetaEngine();

		// Start ThetaEngine
		thetaEngine.connect();
	}

	private void connect() {
		this.connectionManager.connect();
	}

	private void initializeBrokerageHandlers() {
		// Initialize API controller
		IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();
		IbController ibController = ibConnectionHandler;

		// Brokerage specific handlers (wiring abstraction)
		this.brokerConnectionHandler = ibConnectionHandler;
		this.brokerExecutionHandler = new IbExecutionHandler(ibController);
		this.brokerTickSubscriber = new IbTickSubscriber(ibController);
		this.brokerPositionHandler = new IbPositionHandler(ibController);
	}

	private void initializeThetaManagers() {
		this.connectionManager = new ConnectionManager(brokerConnectionHandler);
		this.portfolioManager = new PortfolioManager(brokerPositionHandler);
		this.tickManager = new TickManager(brokerTickSubscriber);
		this.executionManager = new ExecutionManager(brokerExecutionHandler);
	}

	private void registerManagerInterfaces() {
		this.portfolioManager.registerTickMonitor(tickManager);
		this.portfolioManager.registerExecutionMonitor(executionManager);
		this.tickManager.registerExecutor(executionManager);
		this.tickManager.registerPositionProvider(portfolioManager);
	}

	private void attachShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				logger.info("Executing Shutdown Hook");
			}
		});
		logger.info("Shutdown Hook attached");
	}
}
