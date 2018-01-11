package brokers.interactive_brokers;

import java.net.InetSocketAddress;
import brokers.interactive_brokers.connection.IbConnectionHandler;
import brokers.interactive_brokers.execution.IbExecutionHandler;
import brokers.interactive_brokers.portfolio.IbPositionHandler;
import brokers.interactive_brokers.tick.IbTickSubscriber;
import theta.api.ConnectionHandler;
import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.TickSubscriber;

public class IbHandlerFactory {

  private static IbConnectionHandler ibConnectionHandler;

  public static ConnectionHandler buildConnectionHandler(InetSocketAddress brokerGatewaySocketAddress) {
    ibConnectionHandler = new IbConnectionHandler(brokerGatewaySocketAddress);

    return ibConnectionHandler;
  }

  public static PositionHandler buildPortfolioHandler() {
    if (ibConnectionHandler == null) {
      throw new IllegalArgumentException("IbConnectionHandler is null.");
    }
    return new IbPositionHandler(ibConnectionHandler);
  }

  public static TickSubscriber buildTickSubscriber() {
    if (ibConnectionHandler == null) {
      throw new IllegalArgumentException("IbConnectionHandler is null.");
    }
    return new IbTickSubscriber(ibConnectionHandler);
  }

  public static ExecutionHandler buildExecutionHandler() {
    if (ibConnectionHandler == null) {
      throw new IllegalArgumentException("IbConnectionHandler is null.");
    }
    return new IbExecutionHandler(ibConnectionHandler);
  }
}
