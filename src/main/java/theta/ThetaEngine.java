package theta;

import java.lang.invoke.MethodHandles;
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

public class ThetaEngine implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Theta managers
  private final ConnectionManager connectionManager;
  private final PortfolioManager portfolioManager;
  private final TickManager tickManager;
  private final ExecutionManager executionManager;

  private final CompositeDisposable managerDisposables = new CompositeDisposable();

  // Entry point for application
  public static void main(final String[] args) throws UnknownHostException {

    logger.info("Starting ThetaEngine...");

    // Create Theta Engine
    final ThetaEngine thetaEngine =
        new ThetaEngine(ThetaManagerFactory.buildConnectionManager(), ThetaManagerFactory.buildPortfolioManager(),
            ThetaManagerFactory.buildTickManager(), ThetaManagerFactory.buildExecutionManager());

    thetaEngine.run();

    logger.info("Completed startup");
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

    final Disposable portfolioManagerDisposable = startPortfolioManager();
    managerDisposables.add(portfolioManagerDisposable);

    final Disposable tickManagerDisposable = startTickManager();
    managerDisposables.add(tickManagerDisposable);
  }

  private Disposable startPortfolioManager() {

    return connectionManager.connect().ignoreElement().andThen(portfolioManager.startPositionProcessing()).subscribe(

        () -> logger.info("Portfolio Manager has Shutdown"),

        error -> {
          logger.error("Portfolio Manager Error", error);
          shutdown();
        });
  }

  private Disposable startTickManager() {

    return portfolioManager.getPositionEnd().andThen(tickManager.startTickProcessing()).subscribe(

        () -> logger.info("Tick Manager has Shutdown"),

        error -> {
          logger.error("Tick Manager Error", error);
          shutdown();
        });
  }

  public void shutdown() {

    if (!managerDisposables.isDisposed()) {

      logger.info("Calling shutdown for all managers.");
      connectionManager.shutdown();
      portfolioManager.shutdown();
      tickManager.shutdown();
      executionManager.shutdown();

      Schedulers.shutdown();

      logger.info("Disposing of {} Managers", managerDisposables.size());
      managerDisposables.dispose();
    } else {
      logger.warn("Tried to dispose of already disposed of Manager Disposable");
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
