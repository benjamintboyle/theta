package theta.connection.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionAddress;
import theta.connection.domain.ConnectionStatus;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;

import java.lang.invoke.MethodHandles;

@Component
public class DefaultConnectionManager implements ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConnectionHandler connectionHandler;
    private final ConnectionAddress connectionAddress;

    private final ManagerStatus managerStatus =
            ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

    private final Composite connectionDisposables = Disposables.composite();

    public DefaultConnectionManager(ConnectionHandler connectionHandler, ConnectionAddress connectionAddress) {
        getManagerStatus().changeState(ManagerState.RUNNING);
        this.connectionHandler = connectionHandler;
        this.connectionAddress = connectionAddress;
    }

    @Override
    public Mono<ConnectionStatus> connect() {
        logger.info("Connecting to Broker servers...");

        return connectionHandler.connect(connectionAddress.getHostAddress(), connectionAddress.getPort());
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down 'Connection Manager' subsystem");
        getManagerStatus().changeState(ManagerState.STOPPING);
        connectionHandler.disconnect();
        connectionDisposables.dispose();
    }

    public ManagerStatus getManagerStatus() {
        return managerStatus;
    }

}
