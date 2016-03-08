package brokers.interactive_brokers.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;

import theta.api.TickHandler;
import theta.api.TickSubscriber;

public class IbTickSubscriber implements TickSubscriber {
	private final Logger logger = LoggerFactory.getLogger(IbTickSubscriber.class);

	private IbController ibController;

	public IbTickSubscriber(IbController ibController) {
		logger.info("Starting Interactive Brokers Tick Subscriber");
		this.ibController = ibController;
	}

	@Override
	public TickHandler subscribeEquity(String ticker) {
		logger.info("Subscribing to Equity: {}", ticker);
		NewContract contract = new NewContract();
		contract.symbol(ticker);
		contract.exchange("SMART");
		contract.primaryExch("ISLAND");

		IbTickHandler ibTickHandler = new IbTickHandler(ticker);
		logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}", contract);
		this.ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

		return ibTickHandler;
	}

	@Override
	public Boolean unsubscribeEquity(TickHandler tickHandler) {
		logger.info("Unsubscribing from Tick Handler: {}", tickHandler.getTicker());
		// TODO: Figure out how to validate cast, and if cancel was successful
		this.ibController.getController().cancelTopMktData((ITopMktDataHandler) tickHandler);

		return Boolean.TRUE;
	}
}
