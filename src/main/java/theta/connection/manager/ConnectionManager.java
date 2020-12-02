package theta.connection.manager;

import reactor.core.publisher.Mono;
import theta.api.ManagerController;
import theta.connection.domain.ConnectionStatus;

public interface ConnectionManager extends ManagerController {

  Mono<ConnectionStatus> connect();
}
