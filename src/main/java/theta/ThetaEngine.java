package theta;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

public class ThetaEngine implements Callable<String> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Theta managers
  private final ConnectionManager connectionManager = ThetaManagerFactory.buildConnectionManager();
  private final PortfolioManager portfolioManager = ThetaManagerFactory.buildPortfolioManager();
  private final TickManager tickManager = ThetaManagerFactory.buildTickManager();
  private final ExecutionManager executionManager = ThetaManagerFactory.buildExecutionManager();

  private final CompositeDisposable managerDisposables = new CompositeDisposable();

  // Entry point for application
  public static void main(final String[] args) {

    System.out.println("Startup ThetaEngine");
    logger.info("Starting ThetaEngine...");

    // Create Theta Engine
    final ThetaEngine thetaEngine = new ThetaEngine();

    final String status = thetaEngine.call();
    logger.info(status);

    System.out.println("ThetaEngine main thread shutdown. Status: " + status);

  }

  public ThetaEngine() {
    // Register shutdown hook
    attachShutdownHook();

    // Register managers with one another as needed
    registerManagerInterfaces();
  }

  @Override
  public String call() {

    // Connection Manager
    managerDisposables
        .add(Flowable.fromCallable(connectionManager).subscribeOn(ThetaSchedulersFactory.getManagerThread())
            .subscribe((endState) -> logger.info("ConnectionManager state: {}", endState)));

    try {
      Thread.sleep(1000);
    } catch (final InterruptedException e) {
      logger.error("Connection check was interupted", e);
    }

    if (connectionManager.isConnected()) {

      // Portfolio Manager
      managerDisposables
          .add(Flowable.fromCallable(portfolioManager).subscribeOn(ThetaSchedulersFactory.getManagerThread())
              .subscribe((endState) -> logger.info("PortfolioManager state: {}", endState)));
      // Tick Manager
      managerDisposables.add(Flowable.fromCallable(tickManager).subscribeOn(ThetaSchedulersFactory.getManagerThread())
          .subscribe((endState) -> logger.info("TickManager state: {}", endState)));
    }

    return "ThetaEngine completed startup";
  }

  public void shutdown() {

    if (!managerDisposables.isDisposed()) {
      logger.info("Disposing of {} Managers", managerDisposables.size());
      managerDisposables.dispose();
    } else {
      logger.info("Managers already disposed.");
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
    portfolioManager.registerExecutionMonitor(executionManager);
    tickManager.registerExecutor(executionManager);
    tickManager.registerPositionProvider(portfolioManager);

    logger.info("Manager Cross-Registration Complete");
  }
}
