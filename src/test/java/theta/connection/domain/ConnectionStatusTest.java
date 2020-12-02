package theta.connection.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionStatusTest {

    private ConnectionStatus sut;

    @BeforeEach
    void setup() {
        sut = ConnectionStatus.of(ConnectionState.CONNECTED);
    }

    @Test
    void checkState() {
        assertThat(sut.getState()).isEqualTo(ConnectionState.CONNECTED);
    }

    @Test
    void checkTime() {
        assertThat(sut.getTime()).isBetween(Instant.now().minusSeconds(1L), Instant.now());
    }
}
