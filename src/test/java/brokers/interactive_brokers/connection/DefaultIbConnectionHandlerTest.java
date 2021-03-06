package brokers.interactive_brokers.connection;

import brokers.interactive_brokers.connection.controller.IbApiController;
import com.ib.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultIbConnectionHandlerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private ApiController apiController;
    @Mock
    private IbApiController ibApiController;

    private DefaultIbConnectionHandler ibConnectionHandler;

    @BeforeEach
    void setup() {
        when(ibApiController.getController()).thenReturn(apiController);
        ibConnectionHandler = new DefaultIbConnectionHandler(ibApiController);
    }

    @Test
    void testGetController_returnsInstanceOfApiController() {
        assertThat(ibConnectionHandler.getController()).isInstanceOf(ApiController.class);
    }

    @Test
    void testConnect_callsControllerConnectOnce() {
        when(ibApiController.getController()).thenReturn(apiController);
        doNothing().when(apiController).connect(anyString(), anyInt(), anyInt(), eq(null));
        when(ibApiController.getConnectionStatus()).thenReturn(Flux.just());

        ibConnectionHandler.connect("", 0);

        verify(apiController, times(1))
                .connect(anyString(), anyInt(), anyInt(), eq(null));
    }

    @Test
    void testConnect_givenFluxDisconnectConnect_thenReturnConnect() {
        when(ibApiController.getController()).thenReturn(apiController);
        doNothing().when(apiController).connect(anyString(), anyInt(), anyInt(), eq(null));
        when(ibApiController.getConnectionStatus()).thenReturn(
                Flux.just(
                        ConnectionStatus.of(ConnectionState.DISCONNECTED),
                        ConnectionStatus.of(ConnectionState.CONNECTED)));

        Mono<ConnectionStatus> connectionStatus = ibConnectionHandler.connect("", 0);

        StepVerifier.create(connectionStatus)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.CONNECTED))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void disconnect() {
        when(ibApiController.getController()).thenReturn(apiController);
        doNothing().when(apiController).disconnect();
        when(ibApiController.getConnectionStatus()).thenReturn(
                Flux.just(
                        ConnectionStatus.of(ConnectionState.CONNECTED),
                        ConnectionStatus.of(ConnectionState.DISCONNECTED)));

        Mono<ConnectionStatus> disconnectStatus = ibConnectionHandler.disconnect();

        StepVerifier.create(disconnectStatus)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.DISCONNECTED))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }
}
