package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.ThetaSchedulersFactory;
import theta.api.PositionHandler;
import theta.domain.PriceLevel;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.composed.Theta;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;
import theta.domain.option.Option;
import theta.domain.pricelevel.DefaultPriceLevel;
import theta.domain.stock.Stock;
import theta.domain.util.SecurityUtil;
import theta.portfolio.api.PositionProvider;
import theta.portfolio.factory.ThetaTradeFactory;
import theta.tick.api.TickMonitor;

public class PortfolioManager implements PositionProvider {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final PositionHandler positionHandler;
  private TickMonitor monitor;

  // Currently active theta trades
  private final Map<UUID, Theta> thetaIdMap = new ConcurrentHashMap<>();

  // Securities to theta trade map
  private final Map<UUID, Set<UUID>> securityThetaLink = new ConcurrentHashMap<>();

  // Internal Id to Security map
  private final Map<UUID, Security> securityIdMap = new ConcurrentHashMap<>();

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  private final CompositeDisposable portfolioDisposables = new CompositeDisposable();

  public PortfolioManager(PositionHandler positionHandler) {
    getStatus().changeState(ManagerState.STARTING);
    this.positionHandler = positionHandler;
  }

  public Maybe<Security> processPosition() {

    logger.debug("Processing Position {}");

    return Maybe.create(emitter -> {


    });
  }

  public Completable startPositionProcessing() {

    logger.debug("Starting Position Processing");

    return Completable.create(emitter -> {

      final Disposable positionLoggerDisposable = positionHandler.requestPositionsFromBrokerage()
          .map(this::processSecurity)
          .debounce(1000, TimeUnit.MILLISECONDS, ThetaSchedulersFactory.ioThread())
          .subscribe(

              security -> PositionLogger.logPositions(getThetaIdMap(), getSecurityThetaLink(), getSecurityIdMap()),

              exception -> {
                logger.error("Issue with Received Positions from Brokerage", exception);
                emitter.onError(exception);
              },

              () -> {
                getStatus().changeState(ManagerState.SHUTDOWN);
                emitter.onComplete();
              },

              subscription -> {
                getStatus().changeState(ManagerState.RUNNING);
                subscription.request(Long.MAX_VALUE);
              });

      portfolioDisposables.add(positionLoggerDisposable);
    });

  }

  public Completable getPositionEnd() {
    return positionHandler.getPositionEnd();
  }

  @Override
  public List<Theta> providePositions(Ticker ticker) {

    List<Theta> positionsToProvide = getThetaIdMap().values()
        .stream()
        .filter(position -> position.getTicker().equals(ticker))
        .collect(Collectors.toList());

    logger.info("Providing Positions for {}: {}", ticker, positionsToProvide);
    return positionsToProvide;
  }

  private Security processSecurity(Security security) {

    logger.info("Processing Position: {}", security);

    removePositionIfExists(security);

    if (security.getQuantity() != 0) {
      getSecurityIdMap().put(security.getId(), security);
      processPosition(security.getTicker());
    } else {
      getSecurityIdMap().remove(security.getId());
      logger.info("Security not processed due to 0 quantity: {}", security);
    }

    return security;
  }

  // Removes positions if security is contained within it
  private List<PriceLevel> removePositionIfExists(Security security) {

    final List<PriceLevel> removedPriceLevels = new ArrayList<>();

    final Set<UUID> thetaIds = Optional.ofNullable(getSecurityThetaLink().remove(security.getId())).orElse(Set.of());

    for (final UUID thetaId : thetaIds) {
      final Optional<Theta> optionalTheta = Optional.ofNullable(getThetaIdMap().remove(thetaId));

      optionalTheta.map(DefaultPriceLevel::of).ifPresent(priceLevel -> {

        removedPriceLevels.add(priceLevel);

        Theta theta = optionalTheta.get();

        // Remove link/map for call and put and stock associated with ThetaTrade
        removedPriceLevels.addAll(removePositionIfExists(theta.getStock()));
        removedPriceLevels.addAll(removePositionIfExists(theta.getCall()));
        removedPriceLevels.addAll(removePositionIfExists(theta.getPut()));

        logger.info("Removed theta trade: {}, based on security: {}", theta, security);
      });
    }

    return removedPriceLevels;
  }

  private void processPosition(Ticker ticker) {

    // Calculate unallocated call, put, stock
    final List<Stock> unallocatedStocks = getUnallocatedSecuritiesOf(ticker, SecurityType.STOCK).stream()
        .map(stock -> (Stock) stock)
        .collect(Collectors.toList());
    final List<Option> unallocatedCalls = getUnallocatedSecuritiesOf(ticker, SecurityType.CALL).stream()
        .map(call -> (Option) call)
        .collect(Collectors.toList());
    final List<Option> unallocatedPuts = getUnallocatedSecuritiesOf(ticker, SecurityType.PUT).stream()
        .map(put -> (Option) put)
        .collect(Collectors.toList());

    if (!unallocatedStocks.isEmpty() && !unallocatedCalls.isEmpty() && !unallocatedPuts.isEmpty()) {
      ThetaTradeFactory.processThetaTrade(unallocatedStocks, unallocatedCalls, unallocatedPuts)
          .stream()
          .map(this::updateSecurityMaps)
          .distinct()
          .forEach(priceLevel -> monitor.addMonitor(priceLevel));
    }

  }

  private List<Security> getUnallocatedSecuritiesOf(Ticker ticker, SecurityType securityType) {

    final List<Security> unallocatedSecurities = new ArrayList<>();

    final Set<Security> allIdsOfTickerAndSecurityType = getSecurityIdMap().values()
        .stream()
        .filter(otherSecurity -> otherSecurity.getTicker().equals(ticker))
        .filter(otherSecurity -> otherSecurity.getSecurityType().equals(securityType))
        .filter(otherSecurity -> otherSecurity.getQuantity() != 0)
        .collect(Collectors.toSet());

    final Map<UUID, Long> allocatedCountMap = getThetaIdMap().values()
        .stream()
        .filter(theta -> theta.getTicker().equals(ticker))
        .map(theta -> theta.getSecurityOfType(securityType))
        .collect(Collectors.groupingBy(Security::getId, Collectors.summingLong(Security::getQuantity)));


    for (final Security security : allIdsOfTickerAndSecurityType) {

      final long unallocatedQuantity =
          Math.abs(security.getQuantity() - allocatedCountMap.getOrDefault(security.getId(), 0L));
      logger.debug("Calculated {} unallocated securities for {}", unallocatedQuantity, security);

      final Optional<Security> securityWithAdjustedQuantity =
          SecurityUtil.getSecurityWithQuantity(security, unallocatedQuantity);

      if (securityWithAdjustedQuantity.isPresent()) {
        unallocatedSecurities.add(securityWithAdjustedQuantity.get());
      }
    }

    return unallocatedSecurities;
  }

  private PriceLevel updateSecurityMaps(Theta theta) {

    getThetaIdMap().put(theta.getId(), theta);

    final Set<UUID> stockThetaIds = getSecurityThetaLink().getOrDefault(theta.getStock().getId(), new HashSet<>());
    stockThetaIds.add(theta.getId());
    getSecurityThetaLink().put(theta.getStock().getId(), stockThetaIds);

    final Set<UUID> callThetaIds = getSecurityThetaLink().getOrDefault(theta.getCall().getId(), new HashSet<>());
    callThetaIds.add(theta.getId());
    getSecurityThetaLink().put(theta.getCall().getId(), callThetaIds);

    final Set<UUID> putThetaIds = getSecurityThetaLink().getOrDefault(theta.getPut().getId(), new HashSet<>());
    putThetaIds.add(theta.getId());
    getSecurityThetaLink().put(theta.getPut().getId(), putThetaIds);

    return DefaultPriceLevel.of(theta);
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }

  public void registerTickMonitor(TickMonitor monitor) {
    logger.info("Registering Tick Monitor with Portfolio Manager");
    this.monitor = monitor;
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    positionHandler.shutdown();
    portfolioDisposables.dispose();
  }

  private Map<UUID, Theta> getThetaIdMap() {
    return thetaIdMap;
  }

  private Map<UUID, Set<UUID>> getSecurityThetaLink() {
    return securityThetaLink;
  }

  private Map<UUID, Security> getSecurityIdMap() {
    return securityIdMap;
  }

}
