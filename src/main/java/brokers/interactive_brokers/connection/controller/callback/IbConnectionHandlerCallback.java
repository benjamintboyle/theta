package brokers.interactive_brokers.connection.controller.callback;

import com.ib.controller.ApiController;
import reactor.core.publisher.Flux;
import theta.connection.domain.ConnectionStatus;

public interface IbConnectionHandlerCallback extends ApiController.IConnectionHandler {
    Flux<ConnectionStatus> getConnectionStatus();
}
