package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionState;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;

public class ConnectionManager {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final CompositeDisposable connectionDisposables = new CompositeDisposable();

  public ConnectionManager(ConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
    getManagerStatus().changeState(ManagerState.RUNNING);
  }

  public Single<ZonedDateTime> connect() {
    logger.info("Connecting to Broker servers...");
    // TODO: Not the correct solution
    if (connectionHandler != null) {
      return connectionHandler.connect();
    } else {
      return Single.error(new IllegalArgumentException());
    }
  }

  public void shutdown() {
    logger.info("Shutting down 'Connection Manager' subsystem");
    getManagerStatus().changeState(ManagerState.STOPPING);
    connectionHandler.disconnect();
    connectionDisposables.dispose();
  }

  public Single<ZonedDateTime> waitUntil(ConnectionState waitUntilState) {
    return connectionHandler.waitUntil(waitUntilState);
  }

  public ManagerStatus getManagerStatus() {
    return managerStatus;
  }

}
