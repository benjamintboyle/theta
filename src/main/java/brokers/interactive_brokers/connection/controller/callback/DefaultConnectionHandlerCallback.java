package brokers.interactive_brokers.connection.controller.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;
import theta.util.ThetaSchedulersFactory;

import java.util.ArrayList;

@Component
public class DefaultConnectionHandlerCallback implements IbConnectionHandlerCallback {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionHandlerCallback.class);

    private static final Mono<String> MESSAGE_TEMPLATE = Mono.just("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'");
    private final EmitterProcessor<ConnectionStatus> connectionStatusProcessor = EmitterProcessor.create();

    @Override
    public Flux<ConnectionStatus> getConnectionStatus() {
        return connectionStatusProcessor;
    }

    @Override
    public void connected() {
        Mono.just("Connection established...").subscribeOn(ThetaSchedulersFactory.ioThread())
                .subscribe(logger::info);
        connectionStatusProcessor.onNext(ConnectionStatus.of(ConnectionState.CONNECTED));
    }

    @Override
    public void disconnected() {
        Mono.just("Connection disconnected...").subscribeOn(ThetaSchedulersFactory.ioThread())
                .subscribe(logger::info);
        connectionStatusProcessor.onNext(ConnectionStatus.of(ConnectionState.DISCONNECTED));
    }

    // Parameter should not be ArrayList, but this is part of the API from Interactive Brokers
    @Override
    public void accountList(ArrayList<String> accountList) {
        Mono.just(accountList).subscribeOn(ThetaSchedulersFactory.ioThread()).subscribe(
                accounts -> logger.info("Received account list: {}", accounts));
    }

    @Override
    public void error(Exception exception) {
        Mono.just(exception).subscribeOn(ThetaSchedulersFactory.ioThread()).subscribe(
                exceptionMessage -> logger.error("Interactive Brokers Error - ", exception));
        connectionStatusProcessor.onError(exception);
    }

    @Override
    public void message(int id, int messageCode, String message) {
        // FIXME: This very possibly could not be thread safe
        if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
            MESSAGE_TEMPLATE.subscribeOn(ThetaSchedulersFactory.ioThread())
                    .subscribe(template -> logger.info(template, Integer.valueOf(id),
                            Integer.valueOf(messageCode), message));
        } else if (messageCode >= 2100 && messageCode <= 2110) {
            MESSAGE_TEMPLATE.subscribeOn(ThetaSchedulersFactory.ioThread())
                    .subscribe(template -> logger.warn(template, Integer.valueOf(id),
                            Integer.valueOf(messageCode), message));
        } else {
            MESSAGE_TEMPLATE.subscribeOn(ThetaSchedulersFactory.ioThread())
                    .subscribe(template -> logger.error(template, Integer.valueOf(id),
                            Integer.valueOf(messageCode), message));
        }
    }

    @Override
    public void show(String string) {
        Mono.just(string).subscribeOn(ThetaSchedulersFactory.ioThread())
                .subscribe(message -> logger.warn("Interactive Brokers Show - {}", message));
    }
}
