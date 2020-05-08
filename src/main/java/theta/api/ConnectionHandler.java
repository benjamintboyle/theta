package theta.api;

import reactor.core.publisher.Mono;
import theta.connection.domain.ConnectionStatus;

public interface ConnectionHandler {
    Mono<ConnectionStatus> connect();

    Mono<ConnectionStatus> disconnect();
}
