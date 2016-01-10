package brokers.interactive_brokers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IConnectionHandler;

public class IbConnectionHandler implements IConnectionHandler {
	final Logger logger = LoggerFactory.getLogger(IbConnectionHandler.class);

	private ArrayList<String> accountList = new ArrayList<String>();

	public ArrayList<String> getAccountList() {
		return this.accountList;
	}

	@Override
	public void connected() {
		this.logger.info("Connection established...");
	}

	@Override
	public void disconnected() {
		this.logger.info("Disconnected...");
	}

	@Override
	public void accountList(ArrayList<String> list) {
		// TODO Store Account List in meaningful location
		this.logger.info("Received account list: {}", list);

		this.accountList.clear();
		this.accountList.addAll(list);
	}

	@Override
	public void error(Exception e) {
		this.logger.error("Error: ", e);
	}

	@Override
	public void message(int id, int errorCode, String errorMsg) {
		this.logger.info("Message: '{}' '{}' '{}'", id, errorCode, errorMsg);
	}

	@Override
	public void show(String string) {
		this.logger.info("Show: {}", string);
	}
}
