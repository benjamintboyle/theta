package brokers.interactive_brokers.connection;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.IbLogger;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureLatest;
import theta.api.ConnectionHandler;
import theta.connection.domain.ConnectionStatus;

public class IbConnectionHandler implements IbController, ConnectionHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final long WAIT_DELAY_MILLI = 5;

  private final ArrayList<String> accountList = new ArrayList<String>();
  private final ApiController ibController =
      new ApiController(getConnectionHandlerCallback(), new IbLogger(), new IbLogger());

  private FlowableOnBackpressureLatest<ConnectionStatus> connectionFlowable;

  private Boolean connected = Boolean.FALSE;

  private InetSocketAddress brokerGatewayAddress = null;

  public IbConnectionHandler(InetSocketAddress brokerGatewayAddress) {
    logger.info("Starting Interactive Brokers Connection Handler");

    this.brokerGatewayAddress = brokerGatewayAddress;
  }

  @Override
  public ApiController getController() {
    return ibController;
  }

  @Override
  public Boolean connect() {
    logger.info("Connecting to Interactive Brokers Gateway at IP: {}:{} as Client 0",
        brokerGatewayAddress.getAddress().getHostAddress(), brokerGatewayAddress.getPort());

    // Paper Trading port = 7497; Operational Trading port = 7496
    getController().connect(brokerGatewayAddress.getAddress().getHostAddress(),
        brokerGatewayAddress.getPort(), 0, null);



    Instant nextTimeToReport = Instant.now();

    while (!isConnected()) {
      // Only write out "Establishing connection" message every 60 seconds
      if (nextTimeToReport.isBefore(Instant.now())) {
        logger.info("Establishing connection...");
        nextTimeToReport = Instant.now().plusSeconds(10);
      }

      // TODO: Convert to Flowable
      // Pause WAIT_DELAY_MILLI between each check for a connection
      try {
        Thread.sleep(WAIT_DELAY_MILLI);
      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for connection", e);
      }
    }

    return isConnected();
  }

  @Override
  public Boolean disconnect() {
    logger.info("Disconnecting...");
    getController().disconnect();

    while (isConnected()) {
      logger.info("Waiting for disconnect confirmation...");
      try {
        // TODO: Convert to Flowable
        Thread.sleep(WAIT_DELAY_MILLI);
      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for disconnect", e);
      }
    }

    return isConnected();
  }

  @Override
  public Boolean isConnected() {
    return connected;
  }

  public ArrayList<String> getAccountList() {
    return accountList;
  }


  private FlowableEmitter<ConnectionStatus> connectionEmitter(ConnectionStatus state) {
    // connectionEmitter.onNext(state);
    return null;
  }

  private FlowableEmitter<String> accountEmitter(String account) {
    return null;
  }

  private IConnectionHandler getConnectionHandlerCallback() {
    final IConnectionHandler connectionHandler = new IConnectionHandler() {

      @Override
      public void connected() {
        logger.info("Connection established...");
        connected = Boolean.TRUE;
        connectionEmitter(ConnectionStatus.CONNECTED);
      }

      @Override
      public void disconnected() {
        logger.info("Connection disconnected...");
        connected = Boolean.FALSE;
        connectionEmitter(ConnectionStatus.DISCONNECTED);
      }

      @Override
      public void accountList(ArrayList<String> list) {
        logger.info("Received account list: {}", list);
        Flowable.fromIterable(list).subscribe(account -> accountEmitter(account));
      }

      @Override
      public void error(Exception e) {
        logger.error("Interactive Brokers Error - ", e);
      }

      @Override
      public void message(int id, int messageCode, String message) {
        if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
          logger.info("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id,
              messageCode, message);
        } else if (messageCode >= 2100 && messageCode <= 2110) {
          logger.warn("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id,
              messageCode, message);
        } else {
          logger.error("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id,
              messageCode, message);
        }
      }

      @Override
      public void show(String string) {
        logger.warn("Interactive Brokers Show - {}", string);
      }
    };

    return connectionHandler;
  }
}
