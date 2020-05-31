package brokers.interactive_brokers;

import com.ib.controller.ApiConnection.ILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class IbLogger implements ILogger {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String loggerName;

    public IbLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public void log(String logMessage) {
        String message = logMessage.trim();

        if (!message.isEmpty()) {
            logger.info("Interactive Brokers {}: '{}'", loggerName, message);
        }
    }
}
