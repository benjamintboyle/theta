package brokers.interactive_brokers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.Types.SecType;

import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.tick.api.TickObserver;

public class IbTickSubscriber implements TickSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(IbTickSubscriber.class);

	private IbController ibController;
	private Map<String, IbTickHandler> ibTickHandlers = new HashMap<String, IbTickHandler>();

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
				IbStringUtil.contractToString(contract.getContract()));
		this.ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

		this.ibTickHandlers.put(ticker, ibTickHandler);

		return ibTickHandler;
	}

	@Override
	public Boolean unsubscribeEquity(TickHandler tickHandler) {
		logger.info("Unsubscribing from Tick Handler: {}", tickHandler.getTicker());
		this.ibController.getController()
				.cancelTopMktData((ITopMktDataHandler) this.ibTickHandlers.get(tickHandler.getTicker()));

		return Boolean.TRUE;
	}
}
