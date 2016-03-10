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
		logger.info("Starting ThetaEngine...");

		// Register shutdown hook
		this.attachShutdownHook();

		// Initialize Broker interfaces
		this.initializeBrokerageHandlers();

		// Initialize Theta Managers with brokerage interfaces
		this.initializeThetaManagers();

		// Register managers with one another as needed
		this.registerManagerInterfaces();

		// Start ThetaEngine
		this.connect();
	}

	// Entry point for application
	public static void main(String[] args) {
		// Create Theta Engine
		ThetaEngine thetaEngine = new ThetaEngine();
		thetaEngine.run();
	}

	public void run() {
		this.brokerPositionHandler.requestPositionsFromBrokerage();
		while (true) {
			this.portfolioManager.logPositions();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Thread interupted: {}", e);
			}
		}
	}

	private void connect() {
		logger.info("Connecting to Brokerage");
		this.connectionManager.connect();
	}

	private void initializeBrokerageHandlers() {
		logger.info("Initializing Brokerage Handlers");

		// Initialize API controller
		IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();
		IbController ibController = ibConnectionHandler;

		// Brokerage specific handlers (wiring abstraction)
		this.brokerConnectionHandler = ibConnectionHandler;
		this.brokerPositionHandler = new IbPositionHandler(ibController);
		this.brokerTickSubscriber = new IbTickSubscriber(ibController);
		this.brokerExecutionHandler = new IbExecutionHandler(ibController);

		logger.info("Brokerage Handlers Initialization Complete");
	}

	private void initializeThetaManagers() {
		logger.info("Starting ThetaEngine Managers");

		this.connectionManager = new ConnectionManager(brokerConnectionHandler);
		this.portfolioManager = new PortfolioManager(brokerPositionHandler);
		this.tickManager = new TickManager(brokerTickSubscriber);
		this.executionManager = new ExecutionManager(brokerExecutionHandler);

		logger.info("ThetaEngine Manager Startup Complete");
	}

	private void registerManagerInterfaces() {
		logger.info("Starting Manager Cross-Registration");

		this.portfolioManager.registerTickMonitor(tickManager);
		this.portfolioManager.registerExecutionMonitor(executionManager);
		this.tickManager.registerExecutor(executionManager);
		this.tickManager.registerPositionProvider(portfolioManager);

		logger.info("Manager Cross-Registration Complete");
	}

	private void attachShutdownHook() {
		logger.info("Registering Shutdown Hook");

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				logger.info("Executing Shutdown Hook");
			}
		});

		logger.info("Shutdown Hook Registered");
	}
}
