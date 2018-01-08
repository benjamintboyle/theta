package theta;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.connection.domain.ConnectionState;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

public class ThetaEngine implements Callable<String> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Theta managers
  private final ConnectionManager connectionManager;
  private final PortfolioManager portfolioManager;
  private final TickManager tickManager;
  private final ExecutionManager executionManager;

  private final CompositeDisposable managerDisposables = new CompositeDisposable();

  // Entry point for application
  public static void main(final String[] args) {

    System.out.print("Starting ThetaEngine... ");
    logger.info("Starting ThetaEngine...");

    // Create Theta Engine
    final ThetaEngine thetaEngine =
        new ThetaEngine(ThetaManagerFactory.buildConnectionManager(), ThetaManagerFactory.buildPortfolioManager(),
            ThetaManagerFactory.buildTickManager(), ThetaManagerFactory.buildExecutionManager());

    final String status = thetaEngine.call();
    logger.info(status);

    System.out.println(status);
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
  public String call() {

    final Disposable connectionDisposable = startConnectionManager();
    managerDisposables.add(connectionDisposable);

    return "Completed startup";
  }

  public void shutdown() {

    if (!managerDisposables.isDisposed()) {
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
    portfolioManager.registerExecutionMonitor(executionManager);

    tickManager.registerPositionProvider(portfolioManager);
    tickManager.registerExecutor(executionManager);

    logger.info("Manager Cross-Registration Complete");
  }

  private Disposable startConnectionManager() {

    return connectionManager.connect().subscribe(

        connectTime -> {
          logger.info("ConnectionManager received CONNECTED confirmation at {}", connectTime);

          final Disposable connectionStatusDisposable = startPortfolioAndTickManagers();
          managerDisposables.add(connectionStatusDisposable);
        },

        error -> {
          logger.error("Issue establishing connection.", error);
        });
  }

  private Disposable startPortfolioAndTickManagers() {

    final Disposable connectionStatusDisposable = connectionManager.waitUntil(ConnectionState.CONNECTED).subscribe(

        connectedTime -> {

          logger.info("Connected at {}. Starting remaining managers.", connectedTime);

          final Disposable positionEndDisposable = portfolioManager.getPositionEnd().subscribe(

              () -> {
                // Tick Manager
                final Disposable tickManagerDisposable = startTickManager();
                managerDisposables.add(tickManagerDisposable);
              },

              error -> {
                logger.error("Error waiting for Position End", error);
                shutdown();
              });
          managerDisposables.add(positionEndDisposable);

          // Portfolio Manager
          final Disposable portfolioManagerDisposable = startPortfolioManager();
          managerDisposables.add(portfolioManagerDisposable);
        },

        connectionError -> {
          logger.error("Connection Manager Error", connectionError);
          shutdown();
        }

    );

    return connectionStatusDisposable;
  }

  private Disposable startPortfolioManager() {

    return Single.fromCallable(portfolioManager).subscribeOn(ThetaSchedulersFactory.getManagerThread()).subscribe(

        endState -> {
          logger.info("{}", endState);
        },

        error -> {
          logger.error("Portfolio Manager Error", error);
          shutdown();
        });
  }

  private Disposable startTickManager() {

    return Single.fromCallable(tickManager).subscribe(

        endState -> {
          logger.info("{}", endState);
        },

        error -> {
          logger.error("Tick Manager Error", error);
          shutdown();
        });
  }
}
