package theta.domain.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ManagerStatusTest {

    private ManagerStatus sut;

    @BeforeEach
    void setup() {
        sut = ManagerStatus.of(getClass(), ManagerState.RUNNING);
    }

    @Test
    void getState() {
        assertThat(sut.getState()).isEqualTo(ManagerState.RUNNING);
    }

    @Test
    void getTime() {
        assertThat(sut.getTime()).isBetween(Instant.now().minusSeconds(1L), Instant.now());
    }

    @Test
    void changeState() {
        sut.changeState(ManagerState.STOPPING);
        assertThat(sut.getState()).isEqualTo(ManagerState.STOPPING);
    }

    @Test
    void testToString() {
        assertThat(sut.toString()).contains(getClass().getSimpleName(), "RUNNING");
    }
}
