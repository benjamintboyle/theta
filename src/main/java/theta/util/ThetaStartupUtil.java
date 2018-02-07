package theta.util;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.tick.api.TickProcessor;
import theta.tick.processor.BidAskSpreadTickProcessor;

public class ThetaStartupUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Connection Manager Configuration
  private static final String GATEWAY_IP_ADDRESS_DOCKER = "172.17.0.2";
  private static final String ENGINE_IP_ADDRESS_DOCKER = "172.17.0.3";
  // Operational = 7496, Paper: 7497
  private static final int TWS_PORT = 7497;
  // Operational: 4001, Paper: 4002
  private static final int GATEWAY_PORT = 4002;


  // Tick Manager Configuration
  private static final TickProcessor TICK_PROCESSOR = new BidAskSpreadTickProcessor();
  // private static final TickProcessor TICK_PROCESSOR = new LastTickProcessor();


  // Thread Configuration
  private static final String NAME_PREFIX = "Thread-";


  public static void updateThreadName(String newName) {
    final String newNameWithSuffix = NAME_PREFIX + newName;
    final String oldName = Thread.currentThread().getName();

    if (!oldName.equals(newNameWithSuffix)) {
      logger.info("Renaming Thread: '{}' to '{}'", oldName, newNameWithSuffix);

      Thread.currentThread().setName(newNameWithSuffix);
    }
  }

  public static InetSocketAddress getGatewayAddress() throws UnknownHostException {

    InetAddress gatewayAddress;
    int gatewayPort;

    // If running in a Docker container, then assume GATEWAY is also used
    if (InetAddress.getLocalHost().getHostAddress().equals(ENGINE_IP_ADDRESS_DOCKER)) {
      gatewayAddress = InetAddress.getByName(GATEWAY_IP_ADDRESS_DOCKER);
      gatewayPort = GATEWAY_PORT;

      logger.info("Deteched Docker container.");
    } else {
      // If not running in a Docker container assume using TWS on local host
      gatewayAddress = InetAddress.getLoopbackAddress();
      gatewayPort = TWS_PORT;

      logger.info("Detected local loopback gateway.");
    }

    logger.info("Attempting to use Gateway at: {}:{}", gatewayAddress, gatewayPort);

    return new InetSocketAddress(gatewayAddress, gatewayPort);
  }

  public static TickProcessor getTickProcessor() {
    return TICK_PROCESSOR;
  }
}
