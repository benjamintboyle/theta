package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.ManagerState;
import theta.ThetaUtil;
import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutionMonitor;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.portfolio.factory.ThetaTradeFactory;
import theta.tick.api.TickMonitor;

public class PortfolioManager implements Callable<ManagerState>, PortfolioObserver, PositionProvider {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final PositionHandler positionHandler;
  private TickMonitor monitor;
  private ExecutionMonitor executionMonitor;

  // Currently active theta trades
  private final Map<UUID, ThetaTrade> thetaIdMap = new HashMap<>();

  // Internal Id to Security map
  private final Map<UUID, Security> securityIdMap = new HashMap<>();

  // Securities to theta trade map
  private final Map<UUID, Set<UUID>> securityThetaLink = new HashMap<>();

  // Queue of newly received position updates
  private final BlockingQueue<Security> inputQueue = new LinkedBlockingQueue<>();

  private ManagerState managerState = ManagerState.SHUTDOWN;

  public PortfolioManager(PositionHandler positionHandler) {
    logger.info("Starting Portfolio Manager");
    changeState(ManagerState.STARTING);
    this.positionHandler = positionHandler;
    this.positionHandler.subscribePositions(this);
  }

  public PortfolioManager(PositionHandler positionHandler, TickMonitor monitor) {
    this(positionHandler);
    this.monitor = monitor;
  }

  @Override
  public ManagerState call() {
    ThetaUtil.updateThreadName(MethodHandles.lookup().lookupClass().getSimpleName());

    changeState(ManagerState.RUNNING);

    positionHandler.requestPositionsFromBrokerage();

    while (status() == ManagerState.RUNNING) {

      Optional<Security> security = Optional.empty();

      try {
        security = Optional.of(inputQueue.take());
      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for security", e);
      }

      if (security.isPresent()) {
        final Security unboxedSecurity = security.get();

        securityIdMap.put(unboxedSecurity.getId(), unboxedSecurity);

        removePositionIfExists(unboxedSecurity);

        if (unboxedSecurity.getQuantity() != 0) {

          processPosition(unboxedSecurity);

        } else {
          securityIdMap.remove(unboxedSecurity.getId());
          logger.info("Newly received Security not processed due to 0 quantity: {}", unboxedSecurity);
        }
      }
    }

    changeState(ManagerState.SHUTDOWN);

    return status();
  }

  @Override
  public void acceptPosition(Security security) {
    logger.info("Received Position update: {}", security);
    try {
      inputQueue.put(security);
    } catch (final InterruptedException e) {
      logger.error("Interupted before security could be added", e);
    }
  }

  @Override
  public List<ThetaTrade> providePositions(String ticker) {
    logger.info("Providing Positions for: {}", ticker);
    return thetaIdMap.values().stream().filter(position -> position.getTicker().equals(ticker))
        .collect(Collectors.toList());
  }

  private ManagerState changeState(ManagerState newState) {
    final ManagerState oldState = status();

    managerState = newState;

    logger.info("Portfolio Manager transitioned from {} to {}", oldState, status());

    return status();
  }

  public ManagerState status() {
    return managerState;
  }

  public ManagerState shutdown() {
    return changeState(ManagerState.STOPPING);
  }

  // Removes positions if security is contained within it
  private void removePositionIfExists(Security security) {

    final Optional<Set<UUID>> thetaIds = Optional.ofNullable(securityThetaLink.remove(security.getId()));

    for (final UUID thetaId : thetaIds.orElse(Set.of())) {
      final Optional<ThetaTrade> optionalTheta = Optional.ofNullable(thetaIdMap.remove(thetaId));

      optionalTheta.ifPresent(theta -> {
        // Remove link/map for call and put and stock associated with ThetaTrade
        removePositionIfExists(theta.getStock());
        removePositionIfExists(theta.getCall());
        removePositionIfExists(theta.getPut());

        logger.info("Removed theta trade: {}, based on security: {}", theta, security);

        if (!thetaIdMap.values().stream().filter(ticker -> ticker.getTicker().equals(theta.getTicker())).findAny()
            .isPresent()) {

          logger.info("No more theta positions for {}, removing monitor.", theta.getTicker());
          monitor.deleteMonitor(theta);
        }
      });
    }
  }

  private void processPosition(Security security) {

    // Calculate unassigned call, put, stock
    final List<Stock> unassignedStocks = getUnassignedOfSecurity(security.getTicker(), SecurityType.STOCK).stream()
        .map(stock -> (Stock) stock).collect(Collectors.toList());
    final List<Option> unassignedCalls = getUnassignedOfSecurity(security.getTicker(), SecurityType.CALL).stream()
        .map(call -> (Option) call).collect(Collectors.toList());
    final List<Option> unassignedPuts = getUnassignedOfSecurity(security.getTicker(), SecurityType.PUT).stream()
        .map(put -> (Option) put).collect(Collectors.toList());

    List<ThetaTrade> thetas = new ArrayList<>();

    if (unassignedStocks.size() > 0 && unassignedCalls.size() > 0 && unassignedPuts.size() > 0) {
      thetas = ThetaTradeFactory.processThetaTrade(unassignedStocks, unassignedCalls, unassignedPuts);
    }

    for (final ThetaTrade theta : thetas) {

      updateSecurityMaps(theta);

      monitor.addMonitor(theta);
    }

    logPositions();
    executionMonitor.portfolioChange(security);
  }

  private List<Security> getUnassignedOfSecurity(String ticker, SecurityType securityType) {
    final List<UUID> allIdsOfSecurity =
        securityIdMap.values().stream().filter(otherSecurity -> otherSecurity.getQuantity() != 0)
            .filter(otherSecurity -> otherSecurity.getTicker().equals(ticker))
            .filter(otherSecurity -> otherSecurity.getSecurityType().equals(securityType)).map(Security::getId)
            .collect(Collectors.toList());

    final Map<UUID, Double> assignedCountMap =
        thetaIdMap.values().stream().map(theta -> theta.getSecurityOfType(securityType))
            .filter(otherSecurity -> allIdsOfSecurity.stream()
                .anyMatch(allSecurity -> otherSecurity.getId().equals(allSecurity)))
            .collect(Collectors.groupingBy(Security::getId, Collectors.summingDouble(Security::getQuantity)));


    final List<Security> unassignedSecurities = new ArrayList<>();

    for (final UUID securityId : allIdsOfSecurity) {
      final Security security = securityIdMap.get(securityId);

      final double assignedQuantity =
          Optional.ofNullable(assignedCountMap.get(security.getId())).orElse(Double.valueOf(0)).doubleValue();

      final Optional<Security> securityWithAdjustedQuantity = getSecurityWithQuantity(security, assignedQuantity);

      if (securityWithAdjustedQuantity.isPresent()) {
        unassignedSecurities.add(securityWithAdjustedQuantity.get());
      }
    }

    return unassignedSecurities;
  }

  private void updateSecurityMaps(ThetaTrade theta) {

    thetaIdMap.put(theta.getId(), theta);

    final Set<UUID> stockThetaIds = securityThetaLink.getOrDefault(theta.getStock().getId(), new HashSet<>());
    stockThetaIds.add(theta.getId());
    securityThetaLink.put(theta.getStock().getId(), stockThetaIds);

    final Set<UUID> callThetaIds = securityThetaLink.getOrDefault(theta.getCall().getId(), new HashSet<>());
    callThetaIds.add(theta.getId());
    securityThetaLink.put(theta.getCall().getId(), callThetaIds);

    final Set<UUID> putThetaIds = securityThetaLink.getOrDefault(theta.getPut().getId(), new HashSet<>());
    putThetaIds.add(theta.getId());
    securityThetaLink.put(theta.getPut().getId(), putThetaIds);
  }

  private Optional<Security> getSecurityWithQuantity(Security security, Double assignedQuantity) {

    final double unassignedQuantity = security.getQuantity() - assignedQuantity;
    Optional<Security> securityWithQuantity = Optional.empty();

    if (unassignedQuantity != 0) {
      if (security.getSecurityType().equals(SecurityType.STOCK)) {

        securityWithQuantity =
            Optional.of(new Stock(security.getId(), security.getTicker(), unassignedQuantity, security.getPrice()));
      }

      if (security.getSecurityType().equals(SecurityType.CALL) || security.getSecurityType().equals(SecurityType.PUT)) {

        final Option inputOption = (Option) security;

        securityWithQuantity = Optional.of(
            new Option(inputOption.getId(), inputOption.getSecurityType(), inputOption.getTicker(), unassignedQuantity,
                inputOption.getStrikePrice(), inputOption.getExpiration(), inputOption.getAverageTradePrice()));
      }
    }

    return securityWithQuantity;
  }

  public void registerTickMonitor(TickMonitor monitor) {
    logger.info("Registering Tick Monitor with Portfolio Manager");
    this.monitor = monitor;
  }

  public void registerExecutionMonitor(ExecutionMonitor executionMonitor) {
    logger.info("Registering Execution Monitor with Portfolio Manager");
    this.executionMonitor = executionMonitor;
  }

  public void logPositions() {
    for (final ThetaTrade position : thetaIdMap.values()) {
      logger.info("Current position: {}", position);
    }

    for (final Security security : securityIdMap.values().stream()
        .filter(security -> !securityThetaLink.containsKey(security.getId())).collect(Collectors.toList())) {
      logger.info("Current unprocessed security: {}", security);
    }
  }
}
