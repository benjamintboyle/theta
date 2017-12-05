package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.ManagerState;
import theta.api.ConnectionHandler;

public class ConnectionManager implements Callable<ManagerState> {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  private ManagerState managerState = ManagerState.SHUTDOWN;

  public ConnectionManager(ConnectionHandler connectionHandler) {
    logger.info("Starting Connection Manager");
    this.connectionHandler = connectionHandler;
    managerState = ManagerState.STARTING;
  }

  @Override
  public ManagerState call() throws Exception {
    logger.info("Renaming Thread: '{}' to '{}'", Thread.currentThread().getName(),
        MethodHandles.lookup().lookupClass().getSimpleName());
    final String oldThreadName = Thread.currentThread().getName();
    Thread.currentThread()
        .setName(MethodHandles.lookup().lookupClass().getSimpleName() + " Thread");

    connect();

    while (managerState == ManagerState.RUNNING) {
      if (isConnected()) {
        logger.info("Connected to broker.");
      } else {
        logger.warn("Not connected to broker");
      }

      // Convert to Flowable
      Thread.sleep(60000);
    }

    managerState = ManagerState.SHUTDOWN;

    logger.info("Renaming Thread: '{}' to '{}'", Thread.currentThread().getName(), oldThreadName);
    Thread.currentThread().setName(MethodHandles.lookup().lookupClass().getName());

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

  public Boolean isConnected() {
    logger.info("Checking connection status");

    // TODO: Convert to Flowable
    while (managerState == ManagerState.STARTING || managerState == ManagerState.STOPPING) {
      try {
        Thread.sleep(5);
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        logger.error("IsConnected was interupted", e);
      }
    }

    return connectionHandler.isConnected();
  }

  public ManagerState getState() {
    return managerState;
  }
}
