package theta.connection.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.ConnectionHandler;

public class ConnectionManager {
	private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
	private final ConnectionHandler connectionHandler;

	public ConnectionManager(ConnectionHandler connectionHandler) {
		logger.info("Starting Connection Manager");
		this.connectionHandler = connectionHandler;
	}

	public void connect() {
		this.logger.info("Connecting to Broker servers...");
		this.connectionHandler.connect();

	}

	public void shutdown() {
		this.logger.info("Shutting down 'Connection Manager' subsystem");
		this.connectionHandler.disconnect();
	}
}
