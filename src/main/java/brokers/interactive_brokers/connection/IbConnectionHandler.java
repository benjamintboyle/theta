package brokers.interactive_brokers.connection;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.controller.ApiController;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.IbLogger;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import theta.api.ConnectionHandler;
import theta.connection.domain.BrokerageAccount;
import theta.connection.domain.ConnectionState;

public class IbConnectionHandler implements IbController, ConnectionHandler {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String INPUT_LOG_NAME = "Input";
  private static final String OUTPUT_LOG_NAME = "Output";
  private static final int CLIENT_ID = 0;
  private static final long CONNECTION_TIMEOUT_SECONDS = 3;

  private static final IbConnectionHandlerCallback CALLBACK =
      new IbConnectionHandlerCallback(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS));
  private final ApiController ibController =
      new ApiController(CALLBACK, new IbLogger(INPUT_LOG_NAME), new IbLogger(OUTPUT_LOG_NAME));

  private final CompositeDisposable handlerDisposables = new CompositeDisposable();

  private final InetSocketAddress brokerGatewayAddress;

  public IbConnectionHandler(InetSocketAddress brokerGatewayAddress) {
    logger.info("Starting Interactive Brokers Connection Handler"); //$NON-NLS-1$

    this.brokerGatewayAddress = brokerGatewayAddress;
  }

  @Override
  public ApiController getController() {
    return ibController;
  }

  @Override
  public Single<ZonedDateTime> connect() {

    logger.info("Connecting to Interactive Brokers Gateway at IP: {}:{} as Client {}", //$NON-NLS-1$
        brokerGatewayAddress.getAddress().getHostAddress(), Integer.valueOf(brokerGatewayAddress.getPort()),
        Integer.valueOf(CLIENT_ID));

    getController().connect(brokerGatewayAddress.getAddress().getHostAddress(), brokerGatewayAddress.getPort(),
        CLIENT_ID, null);

    return waitUntil(ConnectionState.CONNECTED);
  }

  @Override
  public Single<ZonedDateTime> disconnect() {

    logger.info("Disconnecting..."); //$NON-NLS-1$

    getController().disconnect();

    shutdown();

    return waitUntil(ConnectionState.DISCONNECTED);
  }

  @Override
  public Single<ZonedDateTime> waitUntil(ConnectionState waitUntilState) {
    return CALLBACK.waitUntil(waitUntilState);
  }

  public static Single<List<BrokerageAccount>> getAccountList() {
    return CALLBACK.getAccountList();
  }

  public void shutdown() {
    CALLBACK.shutdown();
    handlerDisposables.dispose();
  }

}
