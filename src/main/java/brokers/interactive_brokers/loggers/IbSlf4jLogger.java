package brokers.interactive_brokers.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiConnection.ILogger;

import brokers.interactive_brokers.handlers.IbConnectionHandler;

public class IbSlf4jLogger implements ILogger {
	private final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);
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
