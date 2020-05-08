package brokers.interactive_brokers.connection.controller;

import brokers.interactive_brokers.IbLogger;
import brokers.interactive_brokers.connection.controller.callback.IbConnectionHandlerCallback;
import com.ib.controller.ApiController;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import theta.connection.domain.ConnectionStatus;

@Component
public class DefaultIbApiController implements IbApiController {
    private static final String INPUT_LOG_NAME = "API Input";
    private static final String OUTPUT_LOG_NAME = "API Output";

    private final IbConnectionHandlerCallback callback;

    private static DefaultIbApiController INSTANCE = null;
    private static ApiController CONTROLLER = null;

    public DefaultIbApiController(IbConnectionHandlerCallback callback) {
        this.callback = callback;
        CONTROLLER = new ApiController(callback, new IbLogger(INPUT_LOG_NAME),
                new IbLogger(OUTPUT_LOG_NAME));
    }

    @Override
    public Flux<ConnectionStatus> getConnectionStatus() {
        return callback.getConnectionStatus();
    }

    @Override
    public ApiController getController() {
        return CONTROLLER;
    }
}
