package brokers.interactive_brokers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class IbTickUtil {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private IbTickUtil() {
    }

    /**
     * Converts Interactive Brokers TickType to Theta TickType.
     *
     * @param ibTickType Interactive Brokkers TickType received from servers.
     * @return Theta TickType converted from Broker type.
     */
    public static theta.tick.domain.TickType convertToEngineTickType(
            com.ib.client.TickType ibTickType) {

        return switch (ibTickType) {
            case BID -> theta.tick.domain.TickType.BID;
            case ASK -> theta.tick.domain.TickType.ASK;
            case LAST -> theta.tick.domain.TickType.LAST;
            default -> {
                logger.error("Could not convert IB Tick Type enum {} to Engine Tick Type enum", ibTickType);
                throw new IllegalArgumentException("Unknown TickType: " + ibTickType);
            }
        };
    }
}
