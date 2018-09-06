package theta;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;
import theta.util.ThetaStartupUtil;

// @SpringBootApplication
public class ThetaEngine implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String APP_NAME = MethodHandles.lookup().lookupClass().getSimpleName();

  // Managers
  private final ConnectionManager connectionManager;
  private final PortfolioManager portfolioManager;
  private final TickManager tickManager;
  private final ExecutionManager executionManager;

  private final CompositeDisposable managerDisposables = new CompositeDisposable();

  // Entry point for application
  public static void main(final String[] args) throws UnknownHostException {

    logger.info("Starting {}...", APP_NAME);

    // Determine IP address and port of gateway
    final InetSocketAddress brokerGatewaySocketAddress = ThetaStartupUtil.getGatewayAddress();

    // Create and initialized managers
    final ConnectionManager initializedConnectionManager =
        ThetaManagerFactory.buildConnectionManager(brokerGatewaySocketAddress);
    final PortfolioManager initializedPortfolioManager = ThetaManagerFactory.buildPortfolioManager();
    final TickManager initializedTickManager = ThetaManagerFactory.buildTickManager();
    final ExecutionManager initializedExecutionManager = ThetaManagerFactory.buildExecutionManager();

    // Create Theta Engine
    final ThetaEngine thetaEngine = new ThetaEngine(initializedConnectionManager, initializedPortfolioManager,
        initializedTickManager, initializedExecutionManager);

    thetaEngine.run();

    logger.info("{} startup complete.", APP_NAME);
  }

  public ThetaEngine(ConnectionManager connectionManager, PortfolioManager portfolioManager, TickManager tickManager,
      ExecutionManager executionManager) {

    this.connectionManager = connectionManager;
    this.portfolioManager = portfolioManager;
    this.tickManager = tickManager;
    this.executionManager = executionManager;

    // Register shutdown hook
    attachShutdownHook();

    // Register managers with one another as needed
    registerManagerInterfaces();
  }

  @Override
  public void run() {

    // connect -> position process -> tick -> execution -> position

    final Disposable portfolioManagerDisposable = startPortfolioManager();
    managerDisposables.add(portfolioManagerDisposable);

    final Disposable tickManagerDisposable = startTickManager();
    managerDisposables.add(tickManagerDisposable);
  }

  private Disposable startPortfolioManager() {

    return connectionManager.connect()
        .ignoreElement()
        .andThen(portfolioManager.startPositionProcessing())
        .subscribe(

            () -> logger.info("Portfolio Manager has Shutdown"),

            error -> {
              logger.error("Portfolio Manager Error", error);
              shutdown();
            });
  }

  private Disposable startTickManager() {

    return portfolioManager.getPositionEnd()
        .andThen(tickManager.startTickProcessing())
        .subscribe(

            () -> logger.info("Tick Manager has Shutdown"),

            error -> {
              logger.error("Tick Manager Error", error);
              shutdown();
            });
  }

  public void shutdown() {

    if (!managerDisposables.isDisposed()) {

      logger.info("Calling shutdown for all managers.");
      executionManager.shutdown();
      tickManager.shutdown();
      portfolioManager.shutdown();
      connectionManager.shutdown();

      logger.info("Disposing of {} Managers", managerDisposables.size());
      managerDisposables.dispose();

      logger.info("Shutting down Schedulers.");
      Schedulers.shutdown();

    } else {
      logger.warn("Tried to dispose of already disposed of {} Composite Manager Disposable", APP_NAME);
    }

  }

  private void attachShutdownHook() {

    logger.info("Registering Shutdown Hook");

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Executing Shutdown Hook");
      shutdown();
    }));

    logger.info("Shutdown Hook Registered");
  }

  private void registerManagerInterfaces() {

    logger.info("Starting Manager Cross-Registration");

    portfolioManager.registerTickMonitor(tickManager);

    tickManager.registerPositionProvider(portfolioManager);
    tickManager.registerExecutor(executionManager);

    logger.info("Manager Cross-Registration Complete");
  }

}
