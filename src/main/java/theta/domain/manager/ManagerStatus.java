package theta.domain.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ManagerStatus {
    private static final Logger logger = LoggerFactory.getLogger(ManagerStatus.class);

    private final String className;
    private ManagerState state;
    private Instant time;

    private ManagerStatus(Class<?> clazz, ManagerState state, Instant time) {
        className = clazz.getSimpleName();
        this.state = state;
        this.time = time;
    }

    public static ManagerStatus of(Class<?> clazz, ManagerState state) {
        return new ManagerStatus(clazz, state, Instant.now());
    }

    public ManagerState getState() {
        return state;
    }

    public Instant getTime() {
        return time;
    }

    /**
     * Changes the state to input value.
     *
     * @param newState State to be changed to.
     */
    public void changeState(ManagerState newState) {
        logger.info("{} is transitioning from {} to {}", getClassName(), getState(), newState);
        state = newState;
        time = Instant.now();
    }

    private String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return getClassName() +
                " State: " + getState() +
                ", Time: " + getTime();
    }
}
