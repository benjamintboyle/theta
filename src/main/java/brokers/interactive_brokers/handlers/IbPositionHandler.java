package brokers.interactive_brokers.handlers;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.NewContract;

import theta.api.PositionHandler;
import theta.api.SecurityType;
import theta.domain.Option;
import theta.domain.Stock;
import theta.portfolio.api.PortfolioObserver;

public class IbPositionHandler implements IPositionHandler, PositionHandler {
	private final Logger logger = LoggerFactory.getLogger(IbPositionHandler.class);

	private IbController controller;
	private PortfolioObserver portfolioObserver;

	public IbPositionHandler(IbController controller) {
		logger.info("Starting Interactive Brokers Position Handler");
		this.controller = controller;
	}

	@Override
	public synchronized void position(String account, NewContract contract, int position, double avgCost) {
		logger.info(
				"Handler has received position from Brokers servers: Account[{}], Contract[{}], Position[{}], Average Cost[{}]",
				account, contract, position, avgCost);
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
			logger.error("Can not determine Position Type: {}", contract);
			break;
		}
	}

	@Override
	public void positionEnd() {
		logger.info("Received Position End notification");
	}

	@Override
	public void subscribePositions(PortfolioObserver observer) {
		logger.info("Portfolio Manager is observing Handler");
		this.portfolioObserver = observer;
	}

	public void requestPositionsFromBrokerage() {
		logger.info("Requesting Positions from Interactive Brokers");
		this.controller.getController().reqPositions(this);
	}
}
