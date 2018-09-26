package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionState;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;

public class DefaultConnectionManager implements ConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ConnectionHandler connectionHandler;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final CompositeDisposable connectionDisposables = new CompositeDisposable();

  public DefaultConnectionManager(ConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
    getManagerStatus().changeState(ManagerState.RUNNING);
  }

  @Override
  public Single<Instant> connect() {
    logger.info("Connecting to Broker servers...");

    return connectionHandler.connect();
  }

  @Override
  public void shutdown() {
    logger.info("Shutting down 'Connection Manager' subsystem");
    getManagerStatus().changeState(ManagerState.STOPPING);
    connectionHandler.disconnect();
    connectionDisposables.dispose();
  }

  public Single<Instant> waitUntil(ConnectionState waitUntilState) {
    return connectionHandler.waitUntil(waitUntilState);
  }

  public ManagerStatus getManagerStatus() {
    return managerStatus;
  }

}
