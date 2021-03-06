package brokers.interactive_brokers.connection;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.connection.controller.IbApiController;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;

import java.lang.invoke.MethodHandles;

@Component
public class DefaultIbConnectionHandler implements IbController, ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int CLIENT_ID = 0;

    private final IbApiController controller;
    private final Composite DISPOSABLES = Disposables.composite();

    public DefaultIbConnectionHandler(IbApiController ibApiController) {
        controller = ibApiController;
        logger.info("Starting Interactive Brokers Connection Handler");
    }

    @Override
    public ApiController getController() {
        return controller.getController();
    }

    @Override
    public Mono<ConnectionStatus> connect(String connectionHost, int connectionPort) {

        logger.info("Connecting to Interactive Brokers Gateway at IP: {}:{} as Client {}",
                connectionHost, connectionPort, CLIENT_ID);

        getController().connect(connectionHost, connectionPort, CLIENT_ID, null);

        return controller.getConnectionStatus()
                .filter(status -> status.getState().equals(ConnectionState.CONNECTED)).single();
    }

    @Override
    public Mono<ConnectionStatus> disconnect() {

        logger.info("Disconnecting...");

        getController().disconnect();

        return controller.getConnectionStatus()
                .filter(status -> status.getState().equals(ConnectionState.DISCONNECTED))
                .doFirst(this::shutdown).single();
    }

    private void shutdown() {

        if (!DISPOSABLES.isDisposed()) {
            logger.debug("Disposing IbConnectionHandler Disposable");
            DISPOSABLES.dispose();
        } else {
            logger.warn("Tried to dispose of already disposed of IbConnectionHandler Disposable");
        }
    }
}
