package brokers.interactive_brokers.handlers;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;

import theta.api.TickHandler;
import theta.api.TickSubscriber;

public class IbTickSubscriber implements TickSubscriber {

	private IbController ibController;

	public IbTickSubscriber(IbController ibController) {
		this.ibController = ibController;
	}

	@Override
	public TickHandler subscribeEquity(String ticker) {
		NewContract contract = new NewContract();
		contract.symbol(ticker);
		contract.exchange("SMART");
		contract.primaryExch("ISLAND");

		IbTickHandler ibTickHandler = new IbTickHandler();
		this.ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

		return ibTickHandler;
	}

	@Override
	public Boolean unsubscribeEquity(TickHandler tickHandler) {
		// TODO: Figure out how to validate cast, and if cancel was successful
		this.ibController.getController().cancelTopMktData((ITopMktDataHandler) tickHandler);

		return Boolean.TRUE;
	}
}
