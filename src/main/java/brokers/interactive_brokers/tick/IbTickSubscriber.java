package brokers.interactive_brokers.tick;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.contracts.StkContract;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import theta.ThetaSchedulersFactory;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

public class IbTickSubscriber implements TickSubscriber {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Subject<Tick> tickSubject = PublishSubject.create();

  private final IbController ibController;
  private final ConcurrentMap<Ticker, IbTickHandler> ibTickHandlers = new ConcurrentHashMap<>();

  private final CompositeDisposable tickSubscriberDisposables = new CompositeDisposable();

  public IbTickSubscriber(IbController ibController) {
    logger.info("Starting Interactive Brokers Tick Subscriber");
    this.ibController = ibController;
  }

  @Override
  public Flowable<Tick> getTicksAcrossStrikePrices() {
    return tickSubject.serialize().toFlowable(BackpressureStrategy.BUFFER).observeOn(
        ThetaSchedulersFactory.computeThread());
  }

  @Override
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor) {
    Integer remainingPriceLevels = 0;

    Optional<TickHandler> ibTickHandler = getHandler(priceLevel.getTicker());

    if (ibTickHandler.isPresent()) {

      ibTickHandler.get().addPriceLevelMonitor(priceLevel);

      logHandlers();

    } else {

      TickHandler handler = subscribeTick(priceLevel.getTicker(), tickProcessor);

      Disposable handlerDisposable = handler.getTicks().subscribe(

          tickSubject::onNext,

          exception -> logger.error("Error with Tick Handler {}", handler, exception),

          () -> {
            logger.info("Tick Handler cancelled for {}", priceLevel.getTicker());
            unsubscribeTick(priceLevel.getTicker());
          });

      tickSubscriberDisposables.add(handlerDisposable);

      remainingPriceLevels = addPriceLevelMonitor(priceLevel, tickProcessor);
    }

    return remainingPriceLevels;
  }

  @Override
  public Integer removePriceLevelMonitor(PriceLevel priceLevel) {

    Integer remainingPriceLevels = 0;

    Optional<TickHandler> ibLastTickHandler = getHandler(priceLevel.getTicker());

    if (ibLastTickHandler.isPresent()) {

      remainingPriceLevels = ibLastTickHandler.get().removePriceLevelMonitor(priceLevel);

    } else {
      logger.warn("IB Last Tick Handler does not exist for {}", priceLevel.getTicker());
    }

    logHandlers();

    return remainingPriceLevels;
  }

  @Override
  public Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker) {

    Set<PriceLevel> priceLevels = getHandler(ticker).map(TickHandler::getPriceLevelsMonitored).orElse(Set.of());

    if (priceLevels.isEmpty()) {
      logger.warn("No Tick Handler or Price Levels for {}", ticker);
    }

    return priceLevels;
  }

  private TickHandler subscribeTick(Ticker ticker, TickProcessor tickProcessor) {

    logger.info("Subscribing to Ticks for: {}", ticker);
    final StkContract contract = new StkContract(ticker.getSymbol());

    final IbTickHandler ibTickHandler = ibTickHandlers.getOrDefault(ticker, new IbTickHandler(ticker, tickProcessor));
    ibTickHandlers.put(ticker, ibTickHandler);

    logger.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
        IbStringUtil.toStringContract(contract));
    ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

    return ibTickHandler;
  }

  private void unsubscribeTick(Ticker ticker) {

    IbTickHandler ibTickHandler = ibTickHandlers.remove(ticker);

    if (ibTickHandler != null) {

      logger.info("Unsubscribing from Tick Handler: {}", ibTickHandler);

      ibController.getController().cancelTopMktData(ibTickHandler);
      ibTickHandler.cancel();
    } else {
      logger.warn("IB Last Tick Handler does not exist for {}", ticker);
    }
  }

  private Optional<TickHandler> getHandler(Ticker ticker) {

    return Optional.ofNullable(ibTickHandlers.get(ticker));
  }

  private void logHandlers() {

    logger.info("Current Handlers: {}",
        ibTickHandlers.values().stream().sorted(Comparator.comparing(TickHandler::getTicker)).collect(
            Collectors.toList()));
  }

  @Override
  public void unsubscribeAll() {

    for (Ticker ticker : ibTickHandlers.keySet()) {
      unsubscribeTick(ticker);
    }

    tickSubscriberDisposables.dispose();
  }

}
