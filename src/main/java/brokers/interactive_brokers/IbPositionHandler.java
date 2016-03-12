package brokers.interactive_brokers;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
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
				"Handler has received position from Brokers servers: Account[{}], Contract[{}], Position[{}], Average Cost[{}]",
				account, this.contractToString(newContract.getContract()), position, avgCost);
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
			logger.error("Can not determine Position Type: {}", this.contractToString(newContract.getContract()));
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

	@Override
	public void requestPositionsFromBrokerage() {
		logger.info("Requesting Positions from Interactive Brokers");
		this.controller.getController().reqPositions(this);
	}

	private String contractToString(Contract contract) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Contract Id: ");
		stringBuilder.append(contract.m_conId);

		stringBuilder.append(", Symbol: ");
		stringBuilder.append(contract.m_symbol);

		stringBuilder.append(", Security Type: ");
		stringBuilder.append(contract.m_secType);

		stringBuilder.append(", Expiry: ");
		stringBuilder.append(contract.m_expiry);

		stringBuilder.append(", Strike: ");
		stringBuilder.append(contract.m_strike);

		stringBuilder.append(", Right: ");
		stringBuilder.append(contract.m_right);

		stringBuilder.append(", Multiplier: ");
		stringBuilder.append(contract.m_multiplier);

		stringBuilder.append(", Exchange: ");
		stringBuilder.append(contract.m_exchange);

		stringBuilder.append(", Currency: ");
		stringBuilder.append(contract.m_currency);

		stringBuilder.append(", Local Symbol: ");
		stringBuilder.append(contract.m_localSymbol);

		stringBuilder.append(", Trading Class: ");
		stringBuilder.append(contract.m_tradingClass);

		stringBuilder.append(", Primary Exchange: ");
		stringBuilder.append(contract.m_primaryExch);

		stringBuilder.append(", Include Expired: ");
		stringBuilder.append(contract.m_includeExpired);

		stringBuilder.append(", Security Id Type: ");
		stringBuilder.append(contract.m_secIdType);

		stringBuilder.append(", Security Id: ");
		stringBuilder.append(contract.m_secId);

		stringBuilder.append(", Combo Leg Description: ");
		stringBuilder.append(contract.m_comboLegsDescrip);

		stringBuilder.append(", Combo Legs: { ");
		for (ComboLeg leg : contract.m_comboLegs) {
			stringBuilder.append("[Contract Id: ");
			stringBuilder.append(leg.m_conId);

			stringBuilder.append(", Ratio: ");
			stringBuilder.append(leg.m_ratio);

			stringBuilder.append(", Action: ");
			stringBuilder.append(leg.m_action);

			stringBuilder.append(", Exchange: ");
			stringBuilder.append(leg.m_exchange);

			stringBuilder.append(", Open/Close: ");
			stringBuilder.append(leg.m_openClose);

			stringBuilder.append(", Short Sale Slot: ");
			stringBuilder.append(leg.m_shortSaleSlot);

			stringBuilder.append(", Designated Location: ");
			stringBuilder.append(leg.m_designatedLocation);

			stringBuilder.append(", Exempt Code: ");
			stringBuilder.append(leg.m_exemptCode);

			stringBuilder.append("]");
		}

		stringBuilder.append(" }, Under Comp: { ");
		stringBuilder.append("Contract Id: ");
		stringBuilder.append(contract.m_underComp.m_conId);
		stringBuilder.append(", Delta: ");
		stringBuilder.append(contract.m_underComp.m_delta);
		stringBuilder.append(", Price: ");
		stringBuilder.append(contract.m_underComp.m_price);
		stringBuilder.append(" }");

		return stringBuilder.toString();
	}
}
