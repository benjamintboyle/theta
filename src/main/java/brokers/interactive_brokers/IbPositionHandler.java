package brokers.interactive_brokers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.IPositionHandler;

import brokers.interactive_brokers.util.IbOptionUtil;
import brokers.interactive_brokers.util.IbStringUtil;

import com.ib.controller.NewContract;

import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.api.SecurityType;
import theta.portfolio.api.PortfolioObserver;

public class IbPositionHandler implements IPositionHandler, PositionHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbPositionHandler.class);

	private Map<Integer, UUID> contractIdMap = new HashMap<Integer, UUID>();

	private IbController controller;
	private PortfolioObserver portfolioObserver;

	public IbPositionHandler(IbController controller) {
		logger.info("Starting Interactive Brokers Position Handler");
		this.controller = controller;
	}

	private UUID generateId(Integer contractId) {
		UUID uuid = UUID.randomUUID();

		if (this.contractIdMap.containsKey(contractId)) {
			uuid = this.contractIdMap.get(contractId);
		} else {
			this.contractIdMap.put(contractId, uuid);
		}

		return uuid;
	}

	@Override
	public synchronized void position(String account, NewContract newContract, int position, double avgCost) {
		logger.info(
				"Handler has received position from Brokers servers: Account: {}, Contract: [{}], Position: {}, Average Cost: {}",
				account, IbStringUtil.contractToString(newContract.getContract()), position, avgCost);
		switch (newContract.secType()) {
		case STK:
			Stock stock = new Stock(this.generateId(newContract.getContract().m_conId), newContract.symbol(), position,
					avgCost);
			this.portfolioObserver.ingestPosition(stock);

			break;
		case OPT:
			SecurityType securityType = null;
			switch (newContract.right()) {
			case Call:
				securityType = SecurityType.CALL;
				break;
			case Put:
				securityType = SecurityType.PUT;
				break;
			default:
				logger.error("Could not identify Contract Right: {}",
						IbStringUtil.contractToString(newContract.getContract()));
				break;
			}

			Optional<LocalDate> optionalExpiration = IbOptionUtil.convertExpiration(newContract.expiry());

			if (optionalExpiration.isPresent()) {
				Option option = new Option(this.generateId(newContract.getContract().m_conId), securityType,
						newContract.symbol(), position, newContract.strike(), optionalExpiration.get(), avgCost);
				this.portfolioObserver.ingestPosition(option);
			} else {
				logger.error("Invalid Option Expiration");
			}
			break;
		default:
			logger.error("Can not determine Position Type: {}",
					IbStringUtil.contractToString(newContract.getContract()));
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
