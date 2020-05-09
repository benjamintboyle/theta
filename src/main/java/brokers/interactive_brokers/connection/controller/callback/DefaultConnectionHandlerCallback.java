package brokers.interactive_brokers.connection.controller.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;

import java.util.ArrayList;

@Component
public class DefaultConnectionHandlerCallback implements IbConnectionHandlerCallback {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionHandlerCallback.class);

    private static final String MESSAGE_TEMPLATE = "Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'";
    private final EmitterProcessor<ConnectionStatus> connectionStatusProcessor = EmitterProcessor.create();

    @Override
    public Flux<ConnectionStatus> getConnectionStatus() {
        return connectionStatusProcessor;
    }

    @Override
    public void connected() {
        logger.info("Connection established...");
        connectionStatusProcessor.onNext(ConnectionStatus.of(ConnectionState.CONNECTED));
    }

    @Override
    public void disconnected() {
        logger.info("Connection disconnected...");
        connectionStatusProcessor.onNext(ConnectionStatus.of(ConnectionState.DISCONNECTED));
    }

    // Parameter should not be ArrayList, but this is part of the API from Interactive Brokers
    @Override
    public void accountList(ArrayList<String> accountList) {
        logger.info("Received account list: {}", accountList);
    }

    @Override
    public void error(Exception exception) {
        logger.error("Interactive Brokers Error - ", exception);
        connectionStatusProcessor.onError(exception);
    }

    @Override
    public void message(int id, int messageCode, String message) {
        // FIXME: This very possibly could not be thread safe
        if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
            logger.info(MESSAGE_TEMPLATE, id, messageCode, message);
        } else if (messageCode >= 2100 && messageCode <= 2110) {
            logger.warn(MESSAGE_TEMPLATE, id, messageCode, message);
        } else {
            logger.error(MESSAGE_TEMPLATE, id, messageCode, message);
        }
    }

    @Override
    public void show(String string) {
        logger.warn("Interactive Brokers Show - {}", string);
    }
}
