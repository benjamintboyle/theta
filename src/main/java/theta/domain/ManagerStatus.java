package theta.domain;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerStatus {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ManagerState state;
  private ZonedDateTime time;

  private ManagerStatus(ManagerState state, ZonedDateTime time) {
    this.state = state;
    this.time = time;
  }

  public static ManagerStatus of(ManagerState state) {
    return new ManagerStatus(state, ZonedDateTime.now());
  }

  public ManagerState getState() {
    return state;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public void changeState(ManagerState state) {
    logger.info("Manager is transitioning from {} to {}", getState(), state);
    this.state = state;
    time = ZonedDateTime.now();
  }

}
