package theta.connection.manager;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import theta.api.ConnectionHandler;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;

@Component
public class DefaultConnectionManager implements ConnectionManager {

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ConnectionHandler connectionHandler;

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final CompositeDisposable connectionDisposables = new CompositeDisposable();

  public DefaultConnectionManager(ConnectionHandler connectionHandler) {
    getManagerStatus().changeState(ManagerState.RUNNING);
    this.connectionHandler = connectionHandler;
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

  public ManagerStatus getManagerStatus() {
    return managerStatus;
  }

}
