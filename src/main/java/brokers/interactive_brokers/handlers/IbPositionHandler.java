package brokers.interactive_brokers.handlers;

import java.time.LocalDate;

import com.ib.controller.ApiController.IPositionHandler;

import theta.api.PositionHandler;
import theta.api.SecurityType;
import theta.domain.Option;
import theta.domain.Stock;
import theta.portfolio.api.PortfolioObserver;

import com.ib.controller.NewContract;

public class IbPositionHandler implements IPositionHandler, PositionHandler {

	private IbController controller;
	private PortfolioObserver portfolioObserver;

	public IbPositionHandler(IbController controller) {
		this.controller = controller;

		this.controller.getController().reqPositions(this);
	}

	@Override
	public synchronized void position(String account, NewContract contract, int position, double avgCost) {
		switch (contract.secType()) {
		case STK:
			this.portfolioObserver.ingestPosition(new Stock(contract.symbol(), position, avgCost, contract));
			break;
		case OPT:
			LocalDate expiration = Option.convertExpiration(contract.expiry());

			switch (contract.right()) {
			case Call:
				this.portfolioObserver.ingestPosition(
						new Option(SecurityType.CALL, contract.symbol(), position, contract.strike(), expiration));
				break;
			case Put:
				this.portfolioObserver.ingestPosition(
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

	@Override
	public void subscribePositions(PortfolioObserver observer) {
		this.portfolioObserver = observer;
	}
}
