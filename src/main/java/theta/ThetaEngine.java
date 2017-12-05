package theta;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.connection.IbConnectionHandler;
import brokers.interactive_brokers.execution.IbExecutionHandler;
import brokers.interactive_brokers.portfolio.IbPositionHandler;
import brokers.interactive_brokers.tick.IbTickSubscriber;
import io.reactivex.Flowable;
import theta.api.ConnectionHandler;
import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.TickSubscriber;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

public class ThetaEngine {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Brokerage handlers
  private ConnectionHandler brokerConnectionHandler;
  private ExecutionHandler brokerExecutionHandler;
  private PositionHandler brokerPositionHandler;

  private TickSubscriber brokerTickSubscriber;

  // Theta managers
  private ConnectionManager connectionManager;
  private ExecutionManager executionManager;
  private PortfolioManager portfolioManager;
  private TickManager tickManager;


  public ThetaEngine() {
    ThetaEngine.logger.info("Starting ThetaEngine...");

    // Register shutdown hook
    attachShutdownHook();

    // Initialize Broker interfaces
    initializeBrokerageHandlers();

    // Initialize Theta Managers with brokerage interfaces
    initializeThetaManagers();

    // Register managers with one another as needed
    registerManagerInterfaces();
  }

  public void start() {
    ThetaEngine.logger.info("Connecting to Brokerage");
    Flowable.fromCallable(connectionManager).subscribeOn(ThetaExecutorService.getManagerThread())
        .subscribe((endState) -> logger.info("ConnectionManager state: {}", endState));

    if (connectionManager.isConnected()) {
      // Start manager threads
      Flowable.fromCallable(portfolioManager).subscribeOn(ThetaExecutorService.getManagerThread())
          .subscribe((endState) -> logger.info("PortfolioManager state: {}", endState));
      Flowable.fromCallable(tickManager).subscribeOn(ThetaExecutorService.getManagerThread())
          .subscribe((endState) -> logger.info("TickManager state: {}", endState));
    }

    logger.info("Connected ThetaEngine has started all managers");
  }

  private void attachShutdownHook() {
    ThetaEngine.logger.info("Registering Shutdown Hook");

    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> ThetaEngine.logger.info("Executing Shutdown Hook")));

    ThetaEngine.logger.info("Shutdown Hook Registered");
  }

  private void initializeBrokerageHandlers() {
    ThetaEngine.logger.info("Initializing Brokerage Handlers");

    // Initialize API controller
    final IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();
    final IbController ibController = ibConnectionHandler;

    // Brokerage specific handlers (wiring abstraction)
    brokerConnectionHandler = ibConnectionHandler;
    brokerPositionHandler = new IbPositionHandler(ibController);
    brokerTickSubscriber = new IbTickSubscriber(ibController);
    brokerExecutionHandler = new IbExecutionHandler(ibController);

    ThetaEngine.logger.info("Brokerage Handlers Initialization Complete");
  }

  private void initializeThetaManagers() {
    ThetaEngine.logger.info("Starting ThetaEngine Managers");

    connectionManager = new ConnectionManager(brokerConnectionHandler);
    portfolioManager = new PortfolioManager(brokerPositionHandler);
    tickManager = new TickManager(brokerTickSubscriber);
    executionManager = new ExecutionManager(brokerExecutionHandler);

    ThetaEngine.logger.info("ThetaEngine Manager Startup Complete");
  }

  private void registerManagerInterfaces() {
    ThetaEngine.logger.info("Starting Manager Cross-Registration");

    portfolioManager.registerTickMonitor(tickManager);
    portfolioManager.registerExecutionMonitor(executionManager);
    tickManager.registerExecutor(executionManager);
    tickManager.registerPositionProvider(portfolioManager);

    ThetaEngine.logger.info("Manager Cross-Registration Complete");
  }
}
