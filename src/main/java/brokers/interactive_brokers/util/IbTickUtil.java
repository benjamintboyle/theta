package brokers.interactive_brokers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbTickUtil {
    private static final Logger logger = LoggerFactory.getLogger(IbTickUtil.class);

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
            case BID:
                engineTickType = theta.tick.domain.TickType.BID;
                break;
            case ASK:
                engineTickType = theta.tick.domain.TickType.ASK;
                break;
            case LAST:
                engineTickType = theta.tick.domain.TickType.LAST;
                break;
            default:
                logger.error("Could not convert IB Tick Type enum {} to Engine Tick Type enum", ibTickType);
        }

        return engineTickType;
    }
}
