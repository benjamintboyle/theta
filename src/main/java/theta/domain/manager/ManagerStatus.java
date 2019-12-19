package theta.domain.manager;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManagerStatus {

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
    log.info("{} is transitioning from {} to {}", getClassName(), getState(), newState);
    state = newState;
    time = Instant.now();
  }

  private String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(getClassName());
    stringBuilder.append(" State: ");
    stringBuilder.append(getState());
    stringBuilder.append(", Time: ");
    stringBuilder.append(getTime());

    return stringBuilder.toString();
  }
}
