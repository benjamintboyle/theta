package brokers.interactive_brokers.connection.controller;

import com.ib.controller.ApiController;
import reactor.core.publisher.Flux;
import theta.connection.domain.ConnectionStatus;

public interface IbApiController {
    Flux<ConnectionStatus> getConnectionStatus();

    ApiController getController();
}
