package brokers.interactivebrokers.connection;

import com.ib.controller.ApiController.IConnectionHandler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import theta.connection.domain.BrokerageAccount;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;
import theta.connection.domain.DefaultBrokerageAccount;

@Slf4j
public class IbConnectionHandlerCallback implements IConnectionHandler {

  private final Subject<ConnectionStatus> connectionStatus = BehaviorSubject
      .createDefault(ConnectionStatus.of(ConnectionState.DISCONNECTED)).toSerialized();
  private final List<BrokerageAccount> accountList = new ArrayList<>();

  private final Duration timeout;

  private final CompositeDisposable callbackDisposables = new CompositeDisposable();

  public IbConnectionHandlerCallback(Duration timeout) {
    this.timeout = timeout;
  }

  public Single<List<BrokerageAccount>> getAccountList() {

    return waitUntil(ConnectionState.CONNECTED).map(time -> accountList);
  }

  /**
   * Wait until connected.
   *
   * @param waitUntilState If not Connected it won't connect
   * @return Instant of connection from programs perspective
   */
  public Single<Instant> waitUntil(ConnectionState waitUntilState) {

    return connectionStatus.filter(status -> status.getState().equals(waitUntilState))
        .map(ConnectionStatus::getTime).firstOrError()
        .timeout(timeout.getSeconds(), TimeUnit.SECONDS);
  }

  public void shutdown() {

    if (!callbackDisposables.isDisposed()) {
      log.debug("Disposing IbConnectionHandlerCallback Disposable");
      callbackDisposables.dispose();
    } else {
      log.warn("Tried to dispose of already disposed of IbConnectionHandlerCallback Disposable");
    }
  }

  @Override
  public void connected() {
    log.info("Connection established...");
    connectionStatus.onNext(ConnectionStatus.of(ConnectionState.CONNECTED));
  }

  @Override
  public void disconnected() {
    log.info("Connection disconnected...");
    connectionStatus.onNext(ConnectionStatus.of(ConnectionState.DISCONNECTED));
  }

  // Parameter should not be ArrayList, but this is part of the API from Interactive Brokers
  @Override
  public void accountList(ArrayList<String> accountList) {
    log.info("Received account list: {}", accountList);

    this.accountList.clear();

    for (final String account : accountList) {
      this.accountList.add(new DefaultBrokerageAccount(account));
    }
  }

  @Override
  public void error(Exception exception) {
    log.error("Interactive Brokers Error - ", exception);
    connectionStatus.onError(exception);
  }

  @Override
  public void message(int id, int messageCode, String message) {

    final String messageTemplate =
        "Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'";

    if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
      log.info(messageTemplate, Integer.valueOf(id), Integer.valueOf(messageCode), message);
    } else if (messageCode >= 2100 && messageCode <= 2110) {
      log.warn(messageTemplate, Integer.valueOf(id), Integer.valueOf(messageCode), message);
    } else {
      log.error(messageTemplate, Integer.valueOf(id), Integer.valueOf(messageCode), message);
    }
  }

  @Override
  public void show(String string) {
    log.warn("Interactive Brokers Show - {}", string);
  }

}
