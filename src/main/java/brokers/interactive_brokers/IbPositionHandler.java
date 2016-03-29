package brokers.interactive_brokers;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.NewContract;

import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.api.SecurityType;
import theta.portfolio.api.PortfolioObserver;

public class IbPositionHandler implements IPositionHandler, PositionHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbPositionHandler.class);

	private IbController controller;
	private PortfolioObserver portfolioObserver;

	public IbPositionHandler(IbController controller) {
		logger.info("Starting Interactive Brokers Position Handler");
		this.controller = controller;
	}

	@Override
	public synchronized void position(String account, NewContract newContract, int position, double avgCost) {
		logger.info(
				"Handler has received position from Brokers servers: Account: [{}], Contract: [{}], Position: [{}], Average Cost: [{}]",
				account, IbUtil.contractToString(newContract.getContract()), position, avgCost);
		switch (newContract.secType()) {
		case STK:
			this.portfolioObserver.ingestPosition(new Stock(newContract.symbol(), position, avgCost));
			break;
		case OPT:
			LocalDate expiration = Option.convertExpiration(newContract.expiry());

			switch (newContract.right()) {
			case Call:
				this.portfolioObserver.ingestPosition(new Option(SecurityType.CALL, newContract.symbol(), position,
						newContract.strike(), expiration));
				break;
			case Put:
				this.portfolioObserver.ingestPosition(
						new Option(SecurityType.PUT, newContract.symbol(), position, newContract.strike(), expiration));
				break;
			default:
				break;
			}
			break;
		default:
			logger.error("Can not determine Position Type: {}", IbUtil.contractToString(newContract.getContract()));
			break;
		}
	}

	@Override
	public void positionEnd() {
		logger.info("Received Position End notification");
	}

	@Override
	public synchronized void subscribePositions(PortfolioObserver observer) {
		logger.info("Portfolio Manager is observing Handler");
		this.portfolioObserver = observer;
	}

	@Override
	public void requestPositionsFromBrokerage() {
		logger.info("Requesting Positions from Interactive Brokers");
		this.controller.getController().reqPositions(this);
	}
}
