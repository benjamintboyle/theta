package brokers.interactive_brokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiConnection.ILogger;

public class IbSlf4jLogger implements ILogger {
	private static final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);

	private String loggerType = "";

	public IbSlf4jLogger() {
	}

	public IbSlf4jLogger(String loggerType) {
		this.loggerType = loggerType;
	}

	@Override
	public void log(String valueOf) {
		logger.info("{} {}", this.loggerType, valueOf);
	}
}
