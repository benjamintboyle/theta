package theta;

import java.lang.invoke.MethodHandles;
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
import theta.util.ThetaStartupUtil;

public class ThetaManagerFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static ConnectionManager buildConnectionManager(InetSocketAddress brokerGatewaySocketAddress) {
    logger.info("Initializing Connection Manager");

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

    final TickManager tickManager = new TickManager(brokerTickSubscriber, ThetaStartupUtil.getTickProcessor());

    return tickManager;
  }

  public static ExecutionManager buildExecutionManager() {
    logger.info("Initializing Execution Manager");

    final ExecutionHandler brokerExecutionHandler = IbHandlerFactory.buildExecutionHandler();

    final ExecutionManager executionManager = new ExecutionManager(brokerExecutionHandler);

    return executionManager;
  }
}
