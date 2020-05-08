package brokers.interactive_brokers;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IbLoggerTest {

    @Mock
    Appender<ILoggingEvent> appender;
    @Captor
    ArgumentCaptor<ILoggingEvent> captor;

    private IbLogger ibLogger;

    @BeforeEach
    void setup() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
        ibLogger = new IbLogger("Input");
    }

    @Test
    void givenMessage_ThenLogMessage() {
        ibLogger.log("Test message");

        verify(appender, times(1))
                .doAppend(captor.capture());
        assertThat(captor.getValue().getFormattedMessage()).isEqualTo("Interactive Brokers Input: 'Test message'");
    }

    @Test
    void givenEmptyMessage_ThenDoNotLogMessage() {
        ibLogger.log("");

        verify(appender, times(0))
                .doAppend(any());
    }

    @Test
    void givenMessageWithOnlySpaces_ThenDoNotLogMessage() {
        ibLogger.log("    ");

        verify(appender, times(0))
                .doAppend(any());
    }
}
