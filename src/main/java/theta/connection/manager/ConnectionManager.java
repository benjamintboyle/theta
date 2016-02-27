package theta.connection.manager;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;

import brokers.interactive_brokers.handlers.IbConnectionHandler;
import brokers.interactive_brokers.loggers.IbStdoutLogger;
import theta.connection.api.Controllor;

public class ConnectionManager implements Controllor {
	private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
	private final IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();

	private final ApiController ibController = new ApiController(this.ibConnectionHandler, new IbStdoutLogger(),
			new IbStdoutLogger());

	public ConnectionManager(String host, Integer port, Integer clientId) {
		this.logger.info("Starting subsystem: 'Connection Manager'");
		this.logger.info("Connecting to Broker servers...");
		this.controller().connect(host, port, clientId);
	}

	public ApiController controller() {
		return ibController;
	}

	public ArrayList<String> getAccountList() {
		return this.ibConnectionHandler.getAccountList();
	}

	public void shutdown() {
		this.logger.info("Shutting down 'Connection Manager' subsystem");
		this.controller().disconnect();
	}
}
