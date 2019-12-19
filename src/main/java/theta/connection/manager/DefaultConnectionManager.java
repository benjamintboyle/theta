package theta.connection.manager;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import theta.api.ConnectionHandler;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;

@Slf4j
@Component
public class DefaultConnectionManager implements ConnectionManager {

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
    log.info("Connecting to Broker servers...");

    return connectionHandler.connect();
  }

  @Override
  public void shutdown() {
    log.info("Shutting down 'Connection Manager' subsystem");
    getManagerStatus().changeState(ManagerState.STOPPING);
    connectionHandler.disconnect();
    connectionDisposables.dispose();
  }

  public ManagerStatus getManagerStatus() {
    return managerStatus;
  }

}
