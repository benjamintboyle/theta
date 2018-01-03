package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.ManagerState;
import theta.ThetaUtil;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionState;

public class ConnectionManager implements Callable<ManagerState> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  private ManagerState managerState = ManagerState.SHUTDOWN;

  private final CompositeDisposable connectionDisposables = new CompositeDisposable();

  public ConnectionManager(ConnectionHandler connectionHandler) {
    logger.info("Starting Connection Manager");
    this.connectionHandler = connectionHandler;
    changeState(ManagerState.STARTING);
  }

  @Override
  public ManagerState call() throws Exception {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    connect();

    return getState();
  }

  public void connect() {
    logger.info("Connecting to Broker servers...");
    final Disposable disposable = connectionHandler.connect().subscribe(

        connectTime -> {
          logger.info("ConnectionManager received CONNECTED confirmation at {}", connectTime);
          changeState(ManagerState.RUNNING);
        },

        error -> logger.error("Issue establishing connection.", error)

    );

    connectionDisposables.add(disposable);
  }

  public void shutdown() {
    logger.info("Shutting down 'Connection Manager' subsystem");
    changeState(ManagerState.STOPPING);
    connectionHandler.disconnect();
    connectionDisposables.dispose();
  }

  public Single<ZonedDateTime> waitUntil(ConnectionState waitUntilState) {
    return connectionHandler.waitUntil(waitUntilState);
  }

  private void changeState(ManagerState state) {
    logger.info("Connection Manager is transitioning from {} to {}", getState(), state);
    managerState = state;
  }

  public ManagerState getState() {
    return managerState;
  }

}
