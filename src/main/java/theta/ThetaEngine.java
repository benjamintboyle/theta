package theta;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import theta.api.ManagerShutdown;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

// curl -i -X POST http://localhost:8080/actuator/shutdown
@ComponentScan({"theta", "brokers.interactivebrokers"})
@SpringBootApplication
public class ThetaEngine implements CommandLineRunner, ManagerShutdown {

  private static final Class<?> CURRENT_CLASS = MethodHandles.lookup().lookupClass();
  private static final Logger logger = LoggerFactory.getLogger(CURRENT_CLASS);

  @Value("${application.name}")
  private String appName;

  private static final CompositeDisposable THETA_DISPOSABLES = new CompositeDisposable();

  // Managers
  private final ConnectionManager connectionManager;
  private final PortfolioManager portfolioManager;
  private final TickManager tickManager;
  private final ExecutionManager executionManager;

  public static void main(String[] args) {
    SpringApplication.run(CURRENT_CLASS, args);
  }

  /**
   * Main class for project. Manages other managers. All managers must be passed in.
   *
   * @param connectionManager A broker connection manager.
   * @param portfolioManager A broker portfolio manager.
   * @param tickManager A broker tick manager.
   * @param executionManager A broker execution manager.
   */
  public ThetaEngine(ConnectionManager connectionManager, PortfolioManager portfolioManager,
      TickManager tickManager, ExecutionManager executionManager) {

    this.connectionManager = connectionManager;
    this.portfolioManager = portfolioManager;
    this.tickManager = tickManager;
    this.executionManager = executionManager;
  }

  @Override
  public void run(String... args) {

    final Disposable portfolioManagerDisposable = startPortfolioManager();
    THETA_DISPOSABLES.add(portfolioManagerDisposable);

    final Disposable tickManagerDisposable = startTickManager();
    THETA_DISPOSABLES.add(tickManagerDisposable);

  }

  private Disposable startPortfolioManager() {

    return connectionManager.connect().ignoreElement()
        .andThen(portfolioManager.startPositionProcessing()).subscribe(

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

  @Override
  public void shutdown() {

    logger.info("Calling shutdown for all managers.");

    if (!THETA_DISPOSABLES.isDisposed()) {

      executionManager.shutdown();
      tickManager.shutdown();
      portfolioManager.shutdown();
      connectionManager.shutdown();

      logger.info("Disposing of {} Managers", Integer.valueOf(THETA_DISPOSABLES.size()));
      THETA_DISPOSABLES.dispose();

      logger.info("Shutting down Schedulers.");
      Schedulers.shutdown();

    } else {
      logger.warn("Tried to dispose of already disposed of {} Composite Manager Disposable",
          appName);
    }
  }

}
