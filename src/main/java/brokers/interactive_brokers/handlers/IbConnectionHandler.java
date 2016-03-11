package brokers.interactive_brokers.handlers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;

import theta.api.ConnectionHandler;

public class IbConnectionHandler implements IConnectionHandler, IbController, ConnectionHandler {
	final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);

	private ArrayList<String> accountList = new ArrayList<String>();

	private final ApiController ibController = new ApiController(this, new IbSlf4jLogger("Inbound Message Logger"),
			new IbSlf4jLogger("Outbound Message Logger"));

	public IbConnectionHandler() {
		logger.info("Starting Interactive Brokers Connection Handler");
	}

	public ArrayList<String> getAccountList() {
		return this.accountList;
	}

	@Override
	public void connected() {
		this.logger.info("Connection established...");
	}

	@Override
	public void disconnected() {
		logger.info("Disconnected...");
	}

	@Override
	public void accountList(ArrayList<String> list) {
		this.logger.info("Received account list: {}", list);

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
		this.logger.info("Show: {}", string);
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

		return Boolean.TRUE;
	}

	@Override
	public Boolean disconnect() {
		logger.info("Disconnecting...");
		this.getController().disconnect();
		return Boolean.TRUE;
	}
}
