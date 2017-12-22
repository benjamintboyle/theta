package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Flowable;
import theta.ManagerState;
import theta.ThetaUtil;
import theta.api.ConnectionHandler;

public class ConnectionManager implements Callable<ManagerState> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  private ManagerState managerState = ManagerState.SHUTDOWN;

  private static long CONNECTION_CHECK_TIMEOUT_SECONDS = 5;
  private static long CONNECTION_CHECK_REPEAT_MILLI = 1000;

  public ConnectionManager(ConnectionHandler connectionHandler) {
    logger.info("Starting Connection Manager");
    this.connectionHandler = connectionHandler;
    managerState = ManagerState.STARTING;
  }

  @Override
  public ManagerState call() throws Exception {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    connect();

    return managerState;
  }

  public void connect() {
    logger.info("Connecting to Broker servers...");
    connectionHandler.connect();
    managerState = ManagerState.RUNNING;
  }

  public void shutdown() {
    logger.info("Shutting down 'Connection Manager' subsystem");
    managerState = ManagerState.STOPPING;
    connectionHandler.disconnect();
  }

  // TODO: Convert to Flowable
  public Boolean isConnected() {
    final Instant timeout = Instant.now();

    // If state is STARTING or STOPPING, then check every 5 milliseconds for 1 second
    while ((managerState == ManagerState.STARTING || managerState == ManagerState.STOPPING)
        && timeout.plusSeconds(CONNECTION_CHECK_TIMEOUT_SECONDS).isBefore(Instant.now())) {

      logger.info("ConnectionManager is {}. Checking again in 5 milliseconds...", managerState);

      try {
        Thread.sleep(CONNECTION_CHECK_REPEAT_MILLI);
      } catch (final InterruptedException e) {
        logger.error("Connection check was interupted", e);
      }
    }

    return connectionHandler.isConnected();
  }

  public Flowable<ManagerState> isConnectedFlowable() {


    return null;
  }

  public ManagerState getState() {
    return managerState;
  }
}
