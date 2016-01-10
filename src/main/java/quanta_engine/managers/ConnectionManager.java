package quanta_engine.managers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;

import brokers.interactive_brokers.IbConnectionHandler;
import brokers.interactive_brokers.loggers.IbStdoutLogger;

public class ConnectionManager {
	private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
	private final IbConnectionHandler ibConnectionHandler = new IbConnectionHandler();
	private final PropertiesManager propertiesManager = new PropertiesManager("config.properties");

	private final ApiController ibController = new ApiController(this.ibConnectionHandler, new IbStdoutLogger(),
			new IbStdoutLogger());

	public ConnectionManager() {
		new Thread(this.propertiesManager).start();
		this.logger.info("Starting subsystem: 'Connection Manager'");
		this.logger.info("Connecting to Broker servers...");
		this.controller().connect(this.propertiesManager.getProperty("GATEWAY_HOST"),
				Integer.parseInt(this.propertiesManager.getProperty("GATEWAY_PORT")),
				Integer.parseInt(this.propertiesManager.getProperty("CLIENT_ID")));
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
