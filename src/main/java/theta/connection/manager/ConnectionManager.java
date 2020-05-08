package theta.connection.manager;

import reactor.core.publisher.Mono;
import theta.api.ManagerShutdown;
import theta.connection.domain.ConnectionStatus;

public interface ConnectionManager extends ManagerShutdown {

  Mono<ConnectionStatus> connect();
}
