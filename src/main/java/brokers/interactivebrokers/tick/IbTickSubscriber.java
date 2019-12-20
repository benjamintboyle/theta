package brokers.interactivebrokers.tick;

import static theta.util.LazyEvaluation.lazy;

import brokers.interactivebrokers.IbController;
import brokers.interactivebrokers.util.IbStringUtil;
import com.ib.contracts.StkContract;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;

@Slf4j
@Component
public class IbTickSubscriber implements TickSubscriber {

  private final Subject<Tick> tickSubject = PublishSubject.create();

  private final IbController ibController;
  private final ConcurrentMap<Ticker, IbTickHandler> ibTickHandlers = new ConcurrentHashMap<>();

  private final CompositeDisposable tickSubscriberDisposables = new CompositeDisposable();

  @Autowired
  public IbTickSubscriber(IbController ibController) {
    log.info("Starting Interactive Brokers Tick Subscriber");
    this.ibController = ibController;
  }

  @Override
  public Flowable<Tick> getTicksAcrossStrikePrices() {
    return tickSubject.serialize().toFlowable(BackpressureStrategy.BUFFER);
    // (TEMPORARILY disabled to determine thread performance)
    // .observeOn(ThetaSchedulersFactory.computeThread())
  }

  @Override
  public Integer addPriceLevelMonitor(PriceLevel priceLevel, TickProcessor tickProcessor) {
    Integer remainingPriceLevels = 0;

    final Optional<TickHandler> ibTickHandler = getHandler(priceLevel.getTicker());

    if (ibTickHandler.isPresent()) {

      ibTickHandler.get().addPriceLevelMonitor(priceLevel);

      logHandlers();

    } else {

      final TickHandler handler = subscribeTick(priceLevel.getTicker(), tickProcessor);

      final Disposable handlerDisposable = handler.getTicks().subscribe(

          tickSubject::onNext,

          exception -> log.error("Error with Tick Handler {}", handler, exception),

          () -> {
            log.info("Tick Handler cancelled for {}", priceLevel.getTicker());
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

    final Optional<TickHandler> ibLastTickHandler = getHandler(priceLevel.getTicker());

    if (ibLastTickHandler.isPresent()) {

      remainingPriceLevels = ibLastTickHandler.get().removePriceLevelMonitor(priceLevel);

    } else {
      log.warn("IB Last Tick Handler does not exist for {}", priceLevel.getTicker());
    }

    logHandlers();

    return remainingPriceLevels;
  }

  @Override
  public Set<PriceLevel> getPriceLevelsMonitored(Ticker ticker) {

    final Set<PriceLevel> priceLevels =
        getHandler(ticker).map(TickHandler::getPriceLevelsMonitored).orElse(Set.of());

    if (priceLevels.isEmpty()) {
      log.warn("No Tick Handler or Price Levels for {}", ticker);
    }

    return priceLevels;
  }

  private TickHandler subscribeTick(Ticker ticker, TickProcessor tickProcessor) {

    log.info("Subscribing to Ticks for: {}", ticker);
    final StkContract contract = new StkContract(ticker.getSymbol());

    final IbTickHandler ibTickHandler =
        ibTickHandlers.getOrDefault(ticker, new IbTickHandler(ticker, tickProcessor));
    ibTickHandlers.put(ticker, ibTickHandler);

    log.info("Sending Tick Request to Interactive Brokers server for Contract: {}",
        lazy(() -> IbStringUtil.toStringContract(contract)));
    ibController.getController().reqTopMktData(contract, "", false, ibTickHandler);

    return ibTickHandler;
  }

  private void unsubscribeTick(Ticker ticker) {

    final IbTickHandler ibTickHandler = ibTickHandlers.remove(ticker);

    if (ibTickHandler != null) {

      log.info("Unsubscribing from Tick Handler: {}", ibTickHandler);

      ibController.getController().cancelTopMktData(ibTickHandler);
      ibTickHandler.cancel();
    } else {
      log.warn("IB Last Tick Handler does not exist for {}", ticker);
    }
  }

  private Optional<TickHandler> getHandler(Ticker ticker) {

    return Optional.ofNullable(ibTickHandlers.get(ticker));
  }

  private void logHandlers() {

    log.info("Current Handlers: {}", ibTickHandlers.values().stream()
        .sorted(Comparator.comparing(TickHandler::getTicker)).collect(Collectors.toList()));
  }

  @Override
  public void unsubscribeAll() {

    for (final Ticker ticker : ibTickHandlers.keySet()) {
      unsubscribeTick(ticker);
    }

    if (!tickSubscriberDisposables.isDisposed()) {
      log.debug("Disposing IbTickSubscriber Disposable");
      tickSubscriberDisposables.dispose();
    } else {
      log.warn("Tried to dispose of already disposed of IbTickSubscriber Disposable");
    }
  }

}