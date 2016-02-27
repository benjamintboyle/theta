package brokers.interactive_brokers.handlers;

import java.time.LocalDate;

import com.ib.controller.ApiController.IPositionHandler;

import theta.api.SecurityType;
import theta.domain.Option;
import theta.domain.Stock;
import theta.portfolio.api.PortfolioReceiver;

import com.ib.controller.NewContract;

public class IbPositionHandler implements IPositionHandler {

	private PortfolioReceiver callback;

	public IbPositionHandler(PortfolioReceiver callback) {
		this.callback = callback;
	}

	@Override
	public synchronized void position(String account, NewContract contract, int position, double avgCost) {
		switch (contract.secType()) {
		case STK:
			this.callback.ingestPosition(new Stock(contract.symbol(), position, avgCost, contract));
			break;
		case OPT:
			LocalDate expiration = Option.convertExpiration(contract.expiry());

			switch (contract.right()) {
			case Call:
				this.callback.ingestPosition(
						new Option(SecurityType.CALL, contract.symbol(), position, contract.strike(), expiration));
				break;
			case Put:
				this.callback.ingestPosition(
						new Option(SecurityType.PUT, contract.symbol(), position, contract.strike(), expiration));
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void positionEnd() {
		// TODO Auto-generated method stub
	}
}
