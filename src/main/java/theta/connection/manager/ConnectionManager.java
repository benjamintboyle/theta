package theta.connection.manager;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;

import brokers.interactive_brokers.handlers.IbConnectionHandler;
import theta.connection.api.Controller;

public class ConnectionManager implements Controller {
	private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
	private final IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();

	public ConnectionManager(String host, Integer port, Integer clientId) {
		this.logger.info("Starting subsystem: 'Connection Manager'");
		this.logger.info("Connecting to Broker servers...");
		this.getController().connect(host, port, clientId);
	}

	public ApiController getController() {
		return this.ibConnectionHandler.getController();
	}

	public ArrayList<String> getAccountList() {
		return this.ibConnectionHandler.getAccountList();
	}

	public void shutdown() {
		this.logger.info("Shutting down 'Connection Manager' subsystem");
		this.getController().disconnect();
	}
}
