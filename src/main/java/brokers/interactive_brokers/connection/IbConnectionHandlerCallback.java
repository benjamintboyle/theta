package brokers.interactive_brokers.connection;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.controller.ApiController.IConnectionHandler;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import theta.connection.domain.BrokerageAccount;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;
import theta.connection.domain.DefaultBrokerageAccount;

public class IbConnectionHandlerCallback implements IConnectionHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Subject<ConnectionStatus> connectionStatus =
      BehaviorSubject.createDefault(ConnectionStatus.of(ConnectionState.DISCONNECTED)).toSerialized();
  private final List<BrokerageAccount> accountList = new ArrayList<>();

  private final Duration timeout;

  private final CompositeDisposable callbackDisposables = new CompositeDisposable();

  public IbConnectionHandlerCallback(Duration timeout) {
    this.timeout = timeout;
  }

  public Observable<ConnectionStatus> getConnectionStatus() {
    return connectionStatus;
  }

  public Single<List<BrokerageAccount>> getAccountList() {

    return Single.create(source -> {
      final Disposable disposable = waitUntil(ConnectionState.CONNECTED).subscribe(

          timeConnected -> {
            logger.info("As of {}, Account List: {}", timeConnected, accountList);
            source.onSuccess(accountList);
          },

          error -> logger.error("Issue with getting Account List", error)

      );

      callbackDisposables.add(disposable);

    });
  }

  public Single<ZonedDateTime> waitUntil(ConnectionState waitUntilState) {

    return Single.create(source -> getConnectionStatus().filter(status -> status.getState().equals(waitUntilState))
        .firstOrError().timeout(timeout.getSeconds(), TimeUnit.SECONDS).subscribe(

            status -> source.onSuccess(status.getTime()),

            error -> source.onError(error)

    ));
  }

  public void shutdown() {
    callbackDisposables.dispose();
  }

  @Override
  public void connected() {
    logger.info("Connection established...");
    connectionStatus.onNext(ConnectionStatus.of(ConnectionState.CONNECTED));
  }

  @Override
  public void disconnected() {
    logger.info("Connection disconnected...");
    connectionStatus.onNext(ConnectionStatus.of(ConnectionState.DISCONNECTED));
  }

  @Override
  public void accountList(ArrayList<String> list) {
    logger.info("Received account list: {}", list);

    accountList.clear();
    accountList.addAll(list.stream().map(DefaultBrokerageAccount::new).collect(Collectors.toList()));
  }

  @Override
  public void error(Exception e) {
    logger.error("Interactive Brokers Error - ", e);
  }

  @Override
  public void message(int id, int messageCode, String message) {
    if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
      logger.info("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
    } else if (messageCode >= 2100 && messageCode <= 2110) {
      logger.warn("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
    } else {
      logger.error("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
    }
  }

  @Override
  public void show(String string) {
    logger.warn("Interactive Brokers Show - {}", string);
  }

}
