package theta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.Disposable;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import theta.api.ManagerController;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManger;
import theta.tick.manager.TickManager;

import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandles;

// curl -i -X POST http://localhost:8080/actuator/shutdown

@ComponentScan({"theta", "brokers.interactive_brokers"})
@SpringBootApplication
@ConfigurationPropertiesScan
public class ThetaEngine implements CommandLineRunner, ManagerController {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Managers
    private final ConnectionManager connectionManager;
    private final PortfolioManger portfolioManager;
    private final TickManager tickManager;
    private final ExecutionManager executionManager;

    private final Composite thetaDisposables = Disposables.composite();

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
    public ThetaEngine(ConnectionManager connectionManager,
                       PortfolioManger portfolioManager,
                       TickManager tickManager,
                       ExecutionManager executionManager) {
        this.connectionManager = connectionManager;
        this.portfolioManager = portfolioManager;
        this.tickManager = tickManager;
        this.executionManager = executionManager;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting ThetaEngine");

        Disposable portfolioManagerDisposable = startPortfolioManager()
                .subscribe(
                        null,
                        error -> {
                            logger.error("Portfolio Manager terminated with Error", error);
                            shutdown();
                        },
                        () -> logger.info("Portfolio Manager has Shutdown")
                );
        thetaDisposables.add(portfolioManagerDisposable);

        Disposable tickManagerDisposable = startTickManager()
                .subscribe(
                        null,
                        error -> {
                            logger.error("Tick Manager terminated with Error", error);
                            shutdown();
                        },
                        () -> logger.info("Tick Manager has Shutdown")
                );
        thetaDisposables.add(tickManagerDisposable);
    }

    private Mono<Void> startPortfolioManager() {
        return connectionManager.connect()
                .then(portfolioManager.startPositionProcessing());
    }

    private Mono<Void> startTickManager() {
        return tickManager.startTickProcessing();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        logger.info("Calling shutdown for all ThetaEngine managers.");
        if (!thetaDisposables.isDisposed()) {
            executionManager.shutdown();
            tickManager.shutdown();
            portfolioManager.shutdown();
            connectionManager.shutdown();

            logger.info("Disposing of {} Managers", thetaDisposables.size());
            thetaDisposables.dispose();

            logger.info("Shutting down Schedulers.");
            Schedulers.shutdownNow();

            logger.info("ThetaEngine shutdown complete.");
        } else {
            logger.warn("Tried to dispose of already disposed ThetaEngine Disposable");
        }
    }
}
