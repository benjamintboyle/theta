package brokers.interactive_brokers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;

import theta.api.ConnectionHandler;

public class IbConnectionHandler implements IConnectionHandler, IbController, ConnectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);

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
	public void message(int id, int errorCode, String errorMsg) {
		logger.info("Interactive Brokers Message - Id: '{}', Error Code: '{}', Error Message: '{}'", id, errorCode,
				errorMsg);
	}

	@Override
	public void show(String string) {
		logger.info("Show: {}", string);
	}

	@Override
	public ApiController getController() {
		return this.ibController;
	}

	@Override
	public Boolean connect() {
		logger.info("Connecting to Interactive Brokers Gateway at IP: 127.0.0.1:7497 as Client 0");

		// Paper Trading port = 7497; Operational Trading port = 7496
		this.getController().connect("127.0.0.1", 7497, 0);

		while (!this.connected) {
			logger.info("Establishing connection...");
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

		while (this.connected) {
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
