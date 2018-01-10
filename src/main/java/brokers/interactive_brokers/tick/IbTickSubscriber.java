package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.contracts.StkContract;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.tick.api.TickConsumer;

public class IbTickSubscriber implements TickSubscriber {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;
  private final Map<String, IbLastTickHandler> ibTickHandlers = new HashMap<String, IbLastTickHandler>();

  public IbTickSubscriber(IbController ibController) {
    logger.info("Starting Interactive Brokers Tick Subscriber");
    this.ibController = ibController;
  }

  @Override
  public TickHandler subscribeTick(String ticker, TickConsumer tickConsumer) {

    logger.info("Subscribing to Equity: {}", ticker);
    final StkContract contract = new StkContract(ticker);

    final IbLastTickHandler ibTickHandler = new IbLastTickHandler(ticker, tickConsumer);
    ibTickHandlers.put(ticker, ibTickHandler);

    logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
        IbStringUtil.toStringContract(contract));
    ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

    logger.info("Current Monitors: {}", ibTickHandlers.keySet().stream().sorted().collect(Collectors.toList()));

    return ibTickHandler;
  }

  @Override
  public void unsubscribeTick(TickHandler tickHandler) {

    logger.info("Unsubscribing from Tick Handler: {}", tickHandler.getTicker());

    ibController.getController().cancelTopMktData(ibTickHandlers.remove(tickHandler.getTicker()));
  }

  @Override
  public Optional<TickHandler> getHandler(String ticker) {
    return Optional.ofNullable(ibTickHandlers.get(ticker));
  }
}
