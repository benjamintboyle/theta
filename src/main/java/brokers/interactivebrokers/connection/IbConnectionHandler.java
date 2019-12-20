package brokers.interactivebrokers.connection;

import brokers.interactivebrokers.IbController;
import brokers.interactivebrokers.IbLogger;
import com.ib.controller.ApiController;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import theta.api.ConnectionHandler;
import theta.connection.domain.BrokerageAccount;
import theta.connection.domain.ConnectionState;
import theta.util.ThetaStartupUtil;

@Slf4j
@Component
public class IbConnectionHandler implements IbController, ConnectionHandler {

  private static final String INPUT_LOG_NAME = "API Input";
  private static final String OUTPUT_LOG_NAME = "API Output";
  private static final int CLIENT_ID = 0;
  private static final long CONNECTION_TIMEOUT_SECONDS = 3;

  private static final IbConnectionHandlerCallback CALLBACK =
      new IbConnectionHandlerCallback(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));
  private final ApiController ibController =
      new ApiController(CALLBACK, new IbLogger(INPUT_LOG_NAME), new IbLogger(OUTPUT_LOG_NAME));

  private final CompositeDisposable handlerDisposables = new CompositeDisposable();

  private final InetSocketAddress brokerGatewayAddress;

  @Autowired
  public IbConnectionHandler() throws UnknownHostException {
    brokerGatewayAddress = ThetaStartupUtil.getGatewayAddress();
    log.info("Starting Interactive Brokers Connection Handler: {}", brokerGatewayAddress);
  }

  @Override
  public ApiController getController() {
    return ibController;
  }

  @Override
  public Single<Instant> connect() {

    log.info("Connecting to Interactive Brokers Gateway at IP: {}:{} as Client {}",
        brokerGatewayAddress.getAddress().getHostAddress(),
        Integer.valueOf(brokerGatewayAddress.getPort()), Integer.valueOf(CLIENT_ID));

    getController().connect(brokerGatewayAddress.getAddress().getHostAddress(),
        brokerGatewayAddress.getPort(), CLIENT_ID, null);

    return CALLBACK.waitUntil(ConnectionState.CONNECTED);
  }

  @Override
  public Single<Instant> disconnect() {

    log.info("Disconnecting...");

    getController().disconnect();

    Single<Instant> disconnectTime = CALLBACK.waitUntil(ConnectionState.DISCONNECTED);

    shutdown();

    return disconnectTime;
  }

  public static Single<List<BrokerageAccount>> getAccountList() {
    return CALLBACK.getAccountList();
  }

  private void shutdown() {
    CALLBACK.shutdown();

    if (!handlerDisposables.isDisposed()) {
      log.debug("Disposing IbConnectionHandler Disposable");
      handlerDisposables.dispose();
    } else {
      log.warn("Tried to dispose of already disposed of IbConnectionHandler Disposable");
    }
  }

}
