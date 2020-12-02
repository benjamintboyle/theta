package brokers.interactive_brokers.connection.controller;

import brokers.interactive_brokers.connection.controller.callback.IbConnectionHandlerCallback;
import com.ib.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultIbApiControllerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private IbConnectionHandlerCallback mockCallback;

    private DefaultIbApiController ibApiController;

    @BeforeEach
    void setup() {
        ibApiController = new DefaultIbApiController(mockCallback);
    }

    @Test
    void getConnectionStatus() {
        when(mockCallback.getConnectionStatus())
                .thenReturn(Flux.just(
                        ConnectionStatus.of(ConnectionState.CONNECTED),
                        ConnectionStatus.of(ConnectionState.DISCONNECTED)));

        Flux<ConnectionStatus> connectionStatusFlux = ibApiController.getConnectionStatus();

        StepVerifier.create(connectionStatusFlux)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.CONNECTED))
                .expectNextMatches(status -> status.getState().equals(ConnectionState.DISCONNECTED))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void getController() {
        assertThat(ibApiController.getController()).isInstanceOf(ApiController.class);
    }
}
