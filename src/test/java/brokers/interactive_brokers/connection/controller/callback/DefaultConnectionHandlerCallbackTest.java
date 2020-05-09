package brokers.interactive_brokers.connection.controller.callback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultConnectionHandlerCallbackTest {

    private static final Duration DURATION_OF_NO_EVENT = Duration.ofMillis(10L);
    private DefaultConnectionHandlerCallback sut;

    @Mock
    Appender<ILoggingEvent> appender;
    @Captor
    ArgumentCaptor<ILoggingEvent> captor;

    @BeforeEach
    void setup() {
        sut = new DefaultConnectionHandlerCallback();
    }

    @Test
    void getConnectionStatus() {
        sut.connected();
        sut.disconnected();

        Flux<ConnectionStatus> connectionStatusFlux = sut.getConnectionStatus();

        StepVerifier.create(connectionStatusFlux)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.CONNECTED))
                .expectNextMatches(status -> status.getState().equals(ConnectionState.DISCONNECTED))
                .expectNoEvent(DURATION_OF_NO_EVENT);
    }

    @Test
    void connected() {
        sut.connected();

        Flux<ConnectionStatus> connectionStatusFlux = sut.getConnectionStatus();

        StepVerifier.create(connectionStatusFlux)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.CONNECTED))
                .expectNoEvent(DURATION_OF_NO_EVENT);
    }

    @Test
    void disconnected() {
        sut.disconnected();

        Flux<ConnectionStatus> connectionStatusFlux = sut.getConnectionStatus();

        StepVerifier.create(connectionStatusFlux)
                .expectNextMatches(status -> status.getState().equals(ConnectionState.DISCONNECTED))
                .expectNoEvent(DURATION_OF_NO_EVENT);
    }

    @Test
    void accountList() {
        setupLoggingAppender();

        ArrayList<String> arrayList = new ArrayList<>(List.of("FirstAccount", "SecondAccount"));
        sut.accountList(arrayList);

        validateLogging("Received account list: " + arrayList.toString());
    }

    @Test
    void error() {
        sut.error(new RuntimeException());

        Flux<ConnectionStatus> connectionStatusFlux = sut.getConnectionStatus();

        StepVerifier.create(connectionStatusFlux)
                .expectError(RuntimeException.class);
    }

    @Test
    void message_else() {
        setupLoggingAppender();

        sut.message(0, 1, "Test message");
        validateLogging("Interactive Brokers Message - Id: '0', Code: '1', Message: 'Test message'");
    }

    @Test
    void message_1102() {
        setupLoggingAppender();

        sut.message(0, 1102, "Test message");
        validateLogging("Interactive Brokers Message - Id: '0', Code: '1102', Message: 'Test message'");
    }

    @Test
    void message_2100() {
        setupLoggingAppender();

        sut.message(0, 2100, "Test message");
        validateLogging("Interactive Brokers Message - Id: '0', Code: '2100', Message: 'Test message'");
    }

    @Test
    void show() {
        setupLoggingAppender();

        String message = "Show message";
        sut.show(message);

        validateLogging("Interactive Brokers Show - " + message);
    }

    private void setupLoggingAppender() {
        // Add appender to pull in log message
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    private void validateLogging(String message) {
        verify(appender).doAppend(captor.capture());
        assertThat(captor.getValue().getFormattedMessage())
                .isEqualTo(message);
    }
}
