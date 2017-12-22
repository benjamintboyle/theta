package theta;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import brokers.interactive_brokers.IbHandlerFactory;
import theta.api.ConnectionHandler;
import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.TickSubscriber;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

public class ThetaManagerFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Docker first container: 172.17.0.2, Host IP: 127.0.0.1, AWS: ib-gateway
  // private static final String GATEWAY_IP_ADDRESS = "172.17.0.3";
  private static final InetAddress BROKER_GATEWAY_ADDRESS = InetAddress.getLoopbackAddress();
  // Paper Trading port = 7497; Operational Trading port = 7496
  private static final int BROKER_GATEWAY_PORT = 7497;

  public static ConnectionManager buildConnectionManager() {
    logger.info("Initializing Connection Manager");

    // Initialize API controller
    final InetSocketAddress brokerGatewaySocketAddress =
        new InetSocketAddress(BROKER_GATEWAY_ADDRESS, BROKER_GATEWAY_PORT);

    final ConnectionHandler brokerConnectionHandler =
        IbHandlerFactory.buildConnectionHandler(brokerGatewaySocketAddress);

    final ConnectionManager connectionManager = new ConnectionManager(brokerConnectionHandler);

    return connectionManager;
  }

  public static PortfolioManager buildPortfolioManager() {
    logger.info("Initializing Portfolio Manager");

    final PositionHandler brokerPositionHandler = IbHandlerFactory.buildPortfolioHandler();

    final PortfolioManager portfolioManager = new PortfolioManager(brokerPositionHandler);

    return portfolioManager;
  }

  public static TickManager buildTickManager() {
    logger.info("Initializing Tick Manager");

    final TickSubscriber brokerTickSubscriber = IbHandlerFactory.buildTickSubscriber();

    final TickManager tickManager = new TickManager(brokerTickSubscriber);

    return tickManager;
  }

  public static ExecutionManager buildExecutionManager() {
    logger.info("Initializing Execution Manager");

    final ExecutionHandler brokerExecutionHandler = IbHandlerFactory.buildExecutionHandler();

    final ExecutionManager executionManager = new ExecutionManager(brokerExecutionHandler);

    return executionManager;
  }
}
