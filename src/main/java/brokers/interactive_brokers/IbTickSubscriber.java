package brokers.interactive_brokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;

import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.tick.api.TickObserver;

public class IbTickSubscriber implements TickSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(IbTickSubscriber.class);

	private IbController ibController;

	public IbTickSubscriber(IbController ibController) {
		logger.info("Starting Interactive Brokers Tick Subscriber");
		this.ibController = ibController;
	}

	@Override
	public TickHandler subscribeEquity(String ticker, TickObserver tickObserver) {
		logger.info("Subscribing to Equity: {}", ticker);
		NewContract contract = new NewContract();
		contract.symbol(ticker);
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.primaryExch("ISLAND");

		IbTickHandler ibTickHandler = new IbTickHandler(ticker, tickObserver);
		logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
				IbUtil.contractToString(contract.getContract()));
		this.ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

		return ibTickHandler;
	}

	@Override
	public Boolean unsubscribeEquity(TickHandler tickHandler) {
		logger.info("Unsubscribing from Tick Handler: {}", tickHandler.getTicker());
		this.ibController.getController().cancelTopMktData((ITopMktDataHandler) tickHandler);

		return Boolean.TRUE;
	}
}
