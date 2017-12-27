package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.contracts.StkContract;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.tick.api.TickObserver;

public class IbTickSubscriber implements TickSubscriber {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;
  private final Map<String, IbTickHandler> ibTickHandlers = new HashMap<String, IbTickHandler>();

  public IbTickSubscriber(IbController ibController) {
    logger.info("Starting Interactive Brokers Tick Subscriber");
    this.ibController = ibController;
  }

  @Override
  public TickHandler subscribeTick(String ticker, TickObserver tickObserver) {
    logger.info("Subscribing to Equity: {}", ticker);
    final StkContract contract = new StkContract(ticker);

    final IbTickHandler ibTickHandler = new IbTickHandler(ticker, tickObserver);
    logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
        IbStringUtil.toStringContract(contract));
    ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

    ibTickHandlers.put(ticker, ibTickHandler);

    return ibTickHandler;
  }

  @Override
  public Boolean unsubscribeTick(TickHandler tickHandler) {
    logger.info("Unsubscribing from Tick Handler: {}", tickHandler.getTicker());
    ibController.getController().cancelTopMktData(ibTickHandlers.get(tickHandler.getTicker()));

    return Boolean.TRUE;
  }
}
