package theta;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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

  private ThetaManagerFactory() {}

  public static ConnectionManager buildConnectionManager() throws UnknownHostException {
    logger.info("Initializing Connection Manager");

    // Initialize API controller
    final InetSocketAddress brokerGatewaySocketAddress = ThetaStartupUtil.getGatewayAddress();

    final ConnectionHandler brokerConnectionHandler =
        IbHandlerFactory.buildConnectionHandler(brokerGatewaySocketAddress);

    return new ConnectionManager(brokerConnectionHandler);
  }

  public static PortfolioManager buildPortfolioManager() {
    logger.info("Initializing Portfolio Manager");

    final PositionHandler brokerPositionHandler = IbHandlerFactory.buildPortfolioHandler();

    return new PortfolioManager(brokerPositionHandler);
  }

  public static TickManager buildTickManager() {
    logger.info("Initializing Tick Manager");

    final TickSubscriber brokerTickSubscriber = IbHandlerFactory.buildTickSubscriber();

    return new TickManager(brokerTickSubscriber, ThetaStartupUtil.getTickProcessor());
  }

  public static ExecutionManager buildExecutionManager() {
    logger.info("Initializing Execution Manager");

    final ExecutionHandler brokerExecutionHandler = IbHandlerFactory.buildExecutionHandler();

    return new ExecutionManager(brokerExecutionHandler);
  }
}
