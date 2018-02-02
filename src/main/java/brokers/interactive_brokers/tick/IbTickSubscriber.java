package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.contracts.StkContract;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.Ticker;
import theta.tick.api.PriceLevel;
import theta.tick.api.Tick;
import theta.tick.api.TickConsumer;
import theta.tick.domain.LastTick;

public class IbTickSubscriber implements TickSubscriber {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IbController ibController;
  private final Map<Ticker, IbLastTickHandler> ibTickHandlers = new ConcurrentHashMap<>();

  public IbTickSubscriber(IbController ibController) {
    logger.info("Starting Interactive Brokers Tick Subscriber");
    this.ibController = ibController;
  }

  @Override
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickConsumer tickConsumer) {
    Integer remainingPriceLevels = 0;

    Optional<TickHandler> ibLastTickHandler = getHandler(priceLevel.getTicker());

    if (ibLastTickHandler.isPresent()) {
      ibLastTickHandler.get().addPriceLevelMonitor(priceLevel, tickConsumer);
    } else {
      subscribeTick(priceLevel.getTicker(), tickConsumer);
      remainingPriceLevels = addPriceLevelMonitor(priceLevel, tickConsumer);
    }

    logHandlers();

    return remainingPriceLevels;
  }

  @Override
  public Integer removePriceLevelMonitor(PriceLevel priceLevel) {

    Integer remainingPriceLevels = 0;

    Optional<TickHandler> ibLastTickHandler = getHandler(priceLevel.getTicker());

    if (ibLastTickHandler.isPresent()) {
      remainingPriceLevels = ibLastTickHandler.get().removePriceLevelMonitor(priceLevel);

      if (remainingPriceLevels == 0) {
        unsubscribeTick(priceLevel.getTicker());
      }
    } else {
      logger.warn("IB Last Tick Handler does not exist for {}", priceLevel.getTicker());
    }

    logHandlers();

    return remainingPriceLevels;
  }

  @Override
  public List<PriceLevel> getPriceLevelsMonitored(Ticker ticker) {

    List<PriceLevel> priceLevels = new ArrayList<>();
    Optional<TickHandler> optionalTickHandler = getHandler(ticker);

    if (optionalTickHandler.isPresent()) {
      priceLevels = optionalTickHandler.get().getPriceLevelsMonitored(ticker);
    } else {
      logger.warn("No Tick Handler or Price Levels for {}", ticker);
    }

    return priceLevels;
  }

  @Override
  public Optional<Tick> getLastTick(Ticker ticker) {

    Optional<Tick> tick = Optional.empty();

    Optional<TickHandler> optionalTickHandler = getHandler(ticker);

    if (optionalTickHandler.isPresent()) {
      final TickHandler tickHandler = optionalTickHandler.get();
      tick = Optional.of(new LastTick(ticker, tickHandler.getLast(), tickHandler.getBid(), tickHandler.getAsk(),
          tickHandler.getLastTime()));
    } else {
      logger.warn("No Tick Handler for {}", ticker);
    }

    return tick;
  }

  private TickHandler subscribeTick(Ticker ticker, TickConsumer tickConsumer) {

    logger.info("Subscribing to Ticks for: {}", ticker);
    final StkContract contract = new StkContract(ticker.toString());

    final IbLastTickHandler ibTickHandler =
        ibTickHandlers.getOrDefault(ticker, new IbLastTickHandler(ticker, tickConsumer));
    ibTickHandlers.put(ticker, ibTickHandler);

    logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
        IbStringUtil.toStringContract(contract));
    ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

    return ibTickHandler;
  }

  private void unsubscribeTick(Ticker ticker) {

    IbLastTickHandler ibLastTickHandler = ibTickHandlers.remove(ticker);

    if (ibLastTickHandler != null) {

      logger.info("Unsubscribing from Tick Handler: {}", ibLastTickHandler.getTicker());

      ibController.getController().cancelTopMktData(ibLastTickHandler);
    } else {
      logger.warn("IB Last Tick Handler does not exist for {}", ticker);
    }
  }

  private Optional<TickHandler> getHandler(Ticker ticker) {
    return Optional.ofNullable(ibTickHandlers.get(ticker));
  }

  private void logHandlers() {

    logger.info("Current Handlers: {}",
        ibTickHandlers.values().stream().sorted(Comparator.comparing(IbLastTickHandler::getTicker)).collect(
            Collectors.toList()));

  }

}
