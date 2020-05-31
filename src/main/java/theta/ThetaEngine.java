package theta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.Disposable;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.scheduler.Schedulers;
import theta.api.ManagerShutdown;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandles;

// curl -i -X POST http://localhost:8080/actuator/shutdown

@ComponentScan({"theta", "brokers.interactive_brokers"})
@SpringBootApplication
public class ThetaEngine implements CommandLineRunner, ManagerShutdown {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${application.name}")
    private String appName;

    private static final Composite THETA_DISPOSABLES = Disposables.composite();

    // Managers
    private final ConnectionManager connectionManager;
    private final PortfolioManager portfolioManager;
    private final TickManager tickManager;
    private final ExecutionManager executionManager;

    public static void main(String[] args) {
        SpringApplication.run(ThetaEngine.class, args);
    }

    /**
     * Main class for project. Manages other managers. All managers must be passed in.
     *
     * @param connectionManager A broker connection manager.
     * @param portfolioManager  A broker portfolio manager.
     * @param tickManager       A broker tick manager.
     * @param executionManager  A broker execution manager.
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
        logger.info("Starting {}", appName);

        final Disposable portfolioManagerDisposable = startPortfolioManager();
        THETA_DISPOSABLES.add(portfolioManagerDisposable);

        final Disposable tickManagerDisposable = startTickManager();
        THETA_DISPOSABLES.add(tickManagerDisposable);

    }

    private Disposable startPortfolioManager() {

        return connectionManager.connect().ignoreElement()
                .then(portfolioManager.startPositionProcessing()).subscribe(

                        (Void) -> logger.info("Portfolio Manager has Shutdown"),

                        error -> {
                            logger.error("Portfolio Manager Error: ", error);
                            shutdown();
                        });
    }

    private Disposable startTickManager() {
        return tickManager.startTickProcessing().subscribe(
                (Void) -> logger.info("Tick Manager has Shutdown"),
                error -> {
                    logger.error("Tick Manager Error: ", error);
                    shutdown();
                });
    }

    @PreDestroy
    @Override
    public void shutdown() {

        logger.info("Calling shutdown for all {} managers.", appName);

        if (!THETA_DISPOSABLES.isDisposed()) {

            executionManager.shutdown();
            tickManager.shutdown();
            portfolioManager.shutdown();
            connectionManager.shutdown();

            logger.info("Disposing of {} Managers", THETA_DISPOSABLES.size());
            THETA_DISPOSABLES.dispose();

            logger.info("Shutting down Schedulers.");
            Schedulers.shutdownNow();

            logger.info("ThetaEngine shutdown complete.");

        } else {
            logger.warn("Tried to dispose of already disposed {} Disposable", appName);
        }
    }

}
