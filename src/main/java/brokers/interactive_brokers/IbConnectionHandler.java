package brokers.interactive_brokers;

import java.time.Instant;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;

import theta.api.ConnectionHandler;

public class IbConnectionHandler implements IConnectionHandler, IbController, ConnectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);

	// Docker first container: 172.17.0.2, Host IP: 127.0.0.1, AWS: ib-gateway
	private static final String GATEWAY_IP_ADDRESS = "172.17.0.3";
	// Paper Trading port = 7497; Operational Trading port = 7496
	private static final int GATEWAY_PORT = 7497;

	private Boolean connected = Boolean.FALSE;
	private ArrayList<String> accountList = new ArrayList<String>();
	private final ApiController ibController = new ApiController(this, new IbLogger("Inbound"),
			new IbLogger("Outbound"));

	public IbConnectionHandler() {
		logger.info("Starting Interactive Brokers Connection Handler");
	}

	public ArrayList<String> getAccountList() {
		return this.accountList;
	}

	@Override
	public void connected() {
		this.connected = Boolean.TRUE;
		logger.info("Connection established...");
	}

	@Override
	public void disconnected() {
		this.connected = Boolean.FALSE;
		logger.info("Disconnected...");
	}

	@Override
	public void accountList(ArrayList<String> list) {
		logger.info("Received account list: {}", list);

		this.accountList.clear();
		this.accountList.addAll(list);
	}

	@Override
	public void error(Exception e) {
		logger.error("Error: ", e);
	}

	@Override
	public void message(int id, int messageCode, String message) {

		if ((messageCode == 1102) || (messageCode == 2104) || (messageCode == 2106)) {
			logger.info("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
		} else if (messageCode >= 2100 && messageCode <= 2110) {
			logger.warn("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
		} else {
			logger.error("Interactive Brokers Message - Id: '{}', Code: '{}', Message: '{}'", id, messageCode, message);
		}
	}

	@Override
	public void show(String string) {
		logger.warn("Show: {}", string);
	}

	@Override
	public ApiController getController() {
		return this.ibController;
	}

	@Override
	public Boolean connect() {
		logger.info("Connecting to Interactive Brokers Gateway at IP: {}:{} as Client 0", GATEWAY_IP_ADDRESS,
				GATEWAY_PORT);

		// Paper Trading port = 7497; Operational Trading port = 7496
		this.getController().connect(GATEWAY_IP_ADDRESS, GATEWAY_PORT, 0);

		Instant nextTimeToReport = Instant.now();

		while (!this.connected) {
			// Only write out "Establishing connection" message every 60 seconds
			if (nextTimeToReport.isBefore(Instant.now())) {
				logger.info("Establishing connection...");
				nextTimeToReport = Instant.now().plusSeconds(60);
			}

			// Pause 5 milliseconds between each check for a connection
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				logger.error("Interupted while waiting for connection", e);
			}
		}

		return this.connected;
	}

	@Override
	public Boolean disconnect() {
		logger.info("Disconnecting...");
		this.getController().disconnect();

		while (this.isConnected()) {
			logger.info("Waiting for disconnect confirmation...");
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				logger.error("Interupted while waiting for disconnect", e);
			}
		}

		return this.connected;
	}

	@Override
	public Boolean isConnected() {
		logger.info("IB connection is: {}", this.connected);
		return this.connected;
	}
}
