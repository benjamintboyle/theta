package theta.connection.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Component
public class ThetaStartupUtil implements ConnectionAddress {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Reconfigure to use Spring Profile so this method is not necessary.
    // Connection Manager Configuration
    private static final String GATEWAY_IP_ADDRESS_DOCKER = "172.17.0.2";
    private static final String ENGINE_IP_ADDRESS_DOCKER = "172.17.0.3";
    // Operational = 7496, Paper: 7497
    private static final int TWS_PORT = 7497;
    // Operational: 4001, Paper: 4002
    private static final int GATEWAY_PORT = 4002;

    private static InetSocketAddress inetSocketAddress;

    @Override
    public String getHostAddress() {
        return getInetSocketAddress().getAddress().getHostAddress();
    }

    @Override
    public int getPort() {
        return getInetSocketAddress().getPort();
    }

    private static InetSocketAddress getInetSocketAddress() {
        if (inetSocketAddress == null) {
            InetAddress gatewayAddress = null;
            int gatewayPort = 0;

            // If running in a Docker container, then assume GATEWAY is also used
            InetAddress localhost = null;
            try {
                localhost = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                logger.error("Issue getting LocalHost", e);
            }

            if (localhost != null) {
                if (localhost.getHostAddress().equals(ENGINE_IP_ADDRESS_DOCKER)) {
                    try {
                        gatewayAddress = InetAddress.getByName(GATEWAY_IP_ADDRESS_DOCKER);
                    } catch (UnknownHostException e) {
                        logger.error("Issue getting Gateway Address", e);
                    }
                    gatewayPort = GATEWAY_PORT;

                    logger.info("Detached Docker container.");
                } else {
                    // If not running in a Docker container assume using TWS on local host
                    gatewayAddress = InetAddress.getLoopbackAddress();
                    gatewayPort = TWS_PORT;

                    logger.info("Detected local loopback gateway.");
                }
            }

            logger.info("Attempting to use Gateway at: {}:{}", gatewayAddress, gatewayPort);

            inetSocketAddress = new InetSocketAddress(gatewayAddress, gatewayPort);
        }

        return inetSocketAddress;
    }
}
