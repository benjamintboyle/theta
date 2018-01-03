package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.ThetaUtil;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionState;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;

public class ConnectionManager implements Callable<ManagerStatus> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  private final ManagerStatus managerStatus = ManagerStatus.of(ManagerState.SHUTDOWN);

  private final CompositeDisposable connectionDisposables = new CompositeDisposable();

  public ConnectionManager(ConnectionHandler connectionHandler) {
    logger.info("Starting Connection Manager");
    this.connectionHandler = connectionHandler;
    getManagerStatus().changeState(ManagerState.STARTING);
  }

  @Override
  public ManagerStatus call() throws Exception {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    connect();

    return getManagerStatus();
  }

  public void connect() {
    logger.info("Connecting to Broker servers...");
    final Disposable disposable = connectionHandler.connect().subscribe(

        connectTime -> {
          logger.info("ConnectionManager received CONNECTED confirmation at {}", connectTime);
          getManagerStatus().changeState(ManagerState.RUNNING);
        },

        error -> logger.error("Issue establishing connection.", error)

    );

    connectionDisposables.add(disposable);
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
