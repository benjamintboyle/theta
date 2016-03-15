package brokers.interactive_brokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiConnection.ILogger;

public class IbLogger implements ILogger {
	private static final Logger logger = LoggerFactory.getLogger(IbLogger.class);

	private String loggerType = "";

	public IbLogger() {
	}

	public IbLogger(String loggerType) {
		this.loggerType = loggerType;
	}

	@Override
	public void log(String valueOf) {
		logger.info("{} {}", this.loggerType, valueOf);
	}
}
