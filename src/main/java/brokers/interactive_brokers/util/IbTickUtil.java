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

        theta.tick.domain.TickType engineTickType = null;

        switch (ibTickType) {
            case BID -> engineTickType = theta.tick.domain.TickType.BID;
            case ASK -> engineTickType = theta.tick.domain.TickType.ASK;
            case LAST -> engineTickType = theta.tick.domain.TickType.LAST;
            default -> logger.error("Could not convert IB Tick Type enum {} to Engine Tick Type enum", ibTickType);
        }

        return engineTickType;
    }
}
