package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerStatus {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String className;
  private ManagerState state;
  private ZonedDateTime time;

  private ManagerStatus(Class<?> clazz, ManagerState state, ZonedDateTime time) {
    className = clazz.getSimpleName();
    this.state = state;
    this.time = time;
  }

  public static ManagerStatus of(Class<?> clazz, ManagerState state) {
    return new ManagerStatus(clazz, state, ZonedDateTime.now());
  }

  public ManagerState getState() {
    return state;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public void changeState(ManagerState newState) {
    logger.info("{} is transitioning from {} to {}", getClassName(), getState(), newState);
    state = newState;
    time = ZonedDateTime.now();
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
