package brokers.interactive_brokers;

import com.ib.controller.ApiController.IAccountHandler;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ib.controller.Position;

public class IbAccountHandler implements IAccountHandler {

	private BlockingQueue<Position> positionQueue = new LinkedBlockingQueue<Position>();

	public ArrayList<Position> drainPositionQueue() {
		ArrayList<Position> positionArray = new ArrayList<Position>();
		this.positionQueue.drainTo(positionArray);
		return positionArray;
	}

	@Override
	public synchronized void accountValue(String account, String key, String value, String currency) {
		// TODO Auto-generated method stub
	}

	@Override
	public void accountTime(String timeStamp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void accountDownloadEnd(String account) {
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void updatePortfolio(Position position) {
		this.processPosition(position);
	}

	private void processPosition(Position position) {
		switch (position.contract().secType()) {
		case STK:
			break;
		case OPT:
			break;
		default:
			return;
		}
	}
}
