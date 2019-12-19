package theta.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import theta.tick.api.TickProcessor;
import theta.tick.processor.BidAskSpreadTickProcessor;

@Slf4j
public class ThetaStartupUtil {

  // Connection Manager Configuration
  private static final String GATEWAY_IP_ADDRESS_DOCKER = "172.17.0.2";
  private static final String ENGINE_IP_ADDRESS_DOCKER = "172.17.0.3";
  // Operational = 7496, Paper: 7497
  private static final int TWS_PORT = 7497;
  // Operational: 4001, Paper: 4002
  private static final int GATEWAY_PORT = 4002;
  // Tick Manager Configuration
  private static final TickProcessor TICK_PROCESSOR = new BidAskSpreadTickProcessor();
  // Thread Configuration
  private static final String NAME_PREFIX = "Thread-";

  private ThetaStartupUtil() {

  }

  // TODO: Reconfigure to use Spring Profile so this method is not necessary.
  /**
   * Returns correct Gateway address based on where it is running.
   *
   * @return Gateway address based on what machine is being run.
   * @throws UnknownHostException Re-throw exception, if thrown by getLocalHost()
   */
  public static InetSocketAddress getGatewayAddress() throws UnknownHostException {

    InetAddress gatewayAddress;
    int gatewayPort;

    // If running in a Docker container, then assume GATEWAY is also used
    if (InetAddress.getLocalHost().getHostAddress().equals(ENGINE_IP_ADDRESS_DOCKER)) {
      gatewayAddress = InetAddress.getByName(GATEWAY_IP_ADDRESS_DOCKER);
      gatewayPort = GATEWAY_PORT;

      log.info("Deteched Docker container.");
    } else {
      // If not running in a Docker container assume using TWS on local host
      gatewayAddress = InetAddress.getLoopbackAddress();
      gatewayPort = TWS_PORT;

      log.info("Detected local loopback gateway.");
    }

    log.info("Attempting to use Gateway at: {}:{}", gatewayAddress, gatewayPort);

    return new InetSocketAddress(gatewayAddress, gatewayPort);
  }

  public static TickProcessor getTickProcessor() {
    return TICK_PROCESSOR;
  }

  /**
   * Helper method to changes thread name.
   *
   * @param newName Name thread should be changed to
   */
  public static void updateThreadName(String newName) {
    final String newNameWithSuffix = NAME_PREFIX + newName;
    final String oldName = Thread.currentThread().getName();

    if (!oldName.equals(newNameWithSuffix)) {
      log.info("Renaming Thread: '{}' to '{}'", oldName, newNameWithSuffix);

      Thread.currentThread().setName(newNameWithSuffix);
    }
  }

}
