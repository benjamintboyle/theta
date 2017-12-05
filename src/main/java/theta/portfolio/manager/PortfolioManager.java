package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.ManagerState;
import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutionMonitor;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;

public class PortfolioManager
    implements Callable<ManagerState>, PortfolioObserver, PositionProvider {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final PositionHandler positionHandler;
  private Monitor monitor;
  private ExecutionMonitor executionMonitor;
  private final Map<UUID, ThetaTrade> positionMap = new HashMap<UUID, ThetaTrade>();
  private final Map<UUID, Security> securityIdMap = new HashMap<UUID, Security>();
  private final Map<UUID, UUID> securityPositionMap = new HashMap<UUID, UUID>();
  private final BlockingQueue<Security> positionQueue = new LinkedBlockingQueue<Security>();

  private ManagerState managerState = ManagerState.SHUTDOWN;

  public PortfolioManager(PositionHandler positionHandler) {
    logger.info("Starting Portfolio Manager");
    this.positionHandler = positionHandler;
    this.positionHandler.subscribePositions(this);
  }

  public PortfolioManager(PositionHandler positionHandler, Monitor monitor) {
    this(positionHandler);
    this.monitor = monitor;
  }

  @Override
  public ManagerState call() {
    logger.info("Renaming Thread: '{}' to '{}'", Thread.currentThread().getName(),
        MethodHandles.lookup().lookupClass().getSimpleName());
    final String oldThreadName = Thread.currentThread().getName();
    Thread.currentThread()
        .setName(MethodHandles.lookup().lookupClass().getSimpleName() + " Thread");

    managerState = ManagerState.RUNNING;

    positionHandler.requestPositionsFromBrokerage();

    while (managerState == ManagerState.RUNNING) {
      try {
        final Security security = positionQueue.take();

        processPosition(security);
      } catch (final InterruptedException e) {
        logger.error("Interupted while waiting for security", e);
      }
    }

    managerState = ManagerState.SHUTDOWN;

    logger.info("Renaming Thread: '{}' to '{}'", Thread.currentThread().getName(), oldThreadName);
    Thread.currentThread().setName(MethodHandles.lookup().lookupClass().getName());

    return managerState;
  }

  @Override
  public void ingestPosition(Security security) {
    logger.info("Received Position update: {}", security);
    try {
      positionQueue.put(security);
    } catch (final InterruptedException e) {
      logger.error("Interupted before security could be added", e);
    }
  }

  @Override
  public List<ThetaTrade> providePositions(String ticker) {
    logger.info("Providing Positions for: {}", ticker);
    return positionMap.values().stream().filter(position -> position.getTicker().equals(ticker))
        .filter(position -> position.isComplete()).collect(Collectors.toList());
  }

  // Removes positions if security is contained within it
  private void removePositionIfExists(Security security) {
    logger.info("Checking if security is already mapped to a trade position: {}", security);

    if (securityPositionMap.containsKey(security.getId())) {
      final UUID thetaId = securityPositionMap.remove(security.getId());
      logger.info("Removed security from Security-Position Map with Id: {}", thetaId);

      final ThetaTrade theta = positionMap.remove(thetaId);

      // Remove link/map for call and put associated with ThetaTrade
      if (securityPositionMap.containsKey(theta.getCall().getId())) {
        securityPositionMap.remove(theta.getCall().getId());
      }
      if (securityPositionMap.containsKey(theta.getPut().getId())) {
        securityPositionMap.remove(theta.getPut().getId());
      }

      logger.info("Removed theta trade: {}, based on new security: {}", theta, security);

      if (!positionMap.values().stream()
          .filter(ticker -> ticker.getTicker().equals(theta.getTicker())).findAny().isPresent()) {
        logger.info("No more theta positions for {}, removing monitor", theta.getTicker());
        monitor.deleteMonitor(theta);
      }
    } else {
      logger.warn("Security not being monitored: {}", security);
    }
  }

  private void processSecurity(List<Security> stockList, List<Security> callList,
      List<Security> putList) {
    logger.info("Processing stock list: {}, call list: {}, put list: {}", stockList, callList,
        putList);

    final Optional<ThetaTrade> optionalTheta =
        ThetaTrade.of((Stock) stockList.get(0), (Option) callList.get(0), (Option) putList.get(0));
    if (optionalTheta.isPresent()) {
      final ThetaTrade theta = optionalTheta.get();
      positionMap.put(theta.getId(), theta);
      securityPositionMap.put(theta.getEquity().getId(), theta.getId());
      securityPositionMap.put(theta.getCall().getId(), theta.getId());
      securityPositionMap.put(theta.getPut().getId(), theta.getId());
      monitor.addMonitor(theta);
    }
  }

  private void processPosition(Security security) {

    // If security changed from previous value associated with Security's Id
    if (!security.equals(securityIdMap.put(security.getId(), security))) {
      removePositionIfExists(security);

      // process securities into positions
      // SecurityType -> Ticker -> Price -> Security
      final Map<SecurityType, Map<String, Map<Double, List<Security>>>> unassignedSecurities =
          securityIdMap.keySet().stream().filter(id -> !securityPositionMap.containsKey(id))
              .map(id -> securityIdMap.get(id))
              .collect(Collectors.groupingBy(Security::getSecurityType, Collectors
                  .groupingBy(Security::getTicker, Collectors.groupingBy(Security::getPrice))));

      final List<String> allUnassignedStockTickers = unassignedSecurities.entrySet().stream()
          .filter(type -> type.getKey().equals(SecurityType.STOCK)).map(type -> type.getValue())
          .flatMap(ticker -> ticker.keySet().stream()).distinct().collect(Collectors.toList());

      for (final String ticker : allUnassignedStockTickers) {
        // All unassigned stocks for specific ticker
        final List<Security> stockList =
            unassignedSecurities.get(SecurityType.STOCK).get(ticker).entrySet().stream()
                .map(Entry::getValue).flatMap(List::stream).collect(Collectors.toList());

        final List<Double> callPrices = unassignedSecurities.entrySet().stream()
            .filter(type -> type.getKey().equals(SecurityType.CALL))
            .flatMap(type -> type.getValue().entrySet().stream())
            .filter(tickerList -> tickerList.getKey().equals(ticker))
            .map(tickerList -> tickerList.getValue()).flatMap(price -> price.keySet().stream())
            .collect(Collectors.toList());

        for (final Double callPrice : callPrices) {
          final List<Security> callList = unassignedSecurities.entrySet().stream()
              .filter(type -> type.getKey().equals(SecurityType.CALL))
              .flatMap(type -> type.getValue().entrySet().stream())
              .filter(tickerList -> tickerList.getKey().equals(ticker))
              .flatMap(tickerList -> tickerList.getValue().entrySet().stream())
              .filter(price -> price.getKey().equals(callPrice))
              .flatMap(price -> price.getValue().stream()).collect(Collectors.toList());

          final List<Security> putList = unassignedSecurities.entrySet().stream()
              .filter(type -> type.getKey().equals(SecurityType.PUT))
              .flatMap(type -> type.getValue().entrySet().stream())
              .filter(tickerList -> tickerList.getKey().equals(ticker))
              .flatMap(tickerList -> tickerList.getValue().entrySet().stream())
              .filter(price -> price.getKey().equals(callPrice))
              .flatMap(price -> price.getValue().stream()).collect(Collectors.toList());

          if (!putList.isEmpty()) {
            final Double stockQuantityLong = stockList.stream()
                .mapToDouble(stock -> stock.getQuantity()).filter(quantity -> quantity > 0).sum();
            final Double stockQuantityShort = stockList.stream()
                .mapToDouble(stock -> stock.getQuantity()).filter(quantity -> quantity < 0).sum();

            logger.info("{} Long Stock Quantity: {}, {} Short Stock Quantity: {}", ticker,
                stockQuantityLong, ticker, stockQuantityShort);

            // if there is enough stock
            if (stockQuantityLong % 100 == 0 && stockQuantityLong > 0) {
              processSecurity(stockList.stream().filter(stock -> stock.getQuantity() > 0)
                  .collect(Collectors.toList()), callList, putList);
            }
            if (stockQuantityShort % 100 == 0 && stockQuantityShort < 0) {
              processSecurity(stockList.stream().filter(stock -> stock.getQuantity() < 0)
                  .collect(Collectors.toList()), callList, putList);
            }
          }
        }
      }
    } else {
      logger.warn("Security did not change from previous update: {}", security);
    }

    logPositions();
    executionMonitor.portfolioChange(security);
  }

  public void registerTickMonitor(Monitor monitor) {
    logger.info("Registering Tick Monitor with Portfolio Manager");
    this.monitor = monitor;
  }

  public void registerExecutionMonitor(ExecutionMonitor executionMonitor) {
    logger.info("Registering Execution Monitor with Portfolio Manager");
    this.executionMonitor = executionMonitor;
  }

  public void logPositions() {
    for (final ThetaTrade position : positionMap.values()) {
      logger.info("Current position: {}", position);
    }

    for (final Security security : securityIdMap.values().stream()
        .filter(security -> !securityPositionMap.containsKey(security.getId()))
        .collect(Collectors.toList())) {
      logger.info("Current unprocessed security: {}", security);
    }
  }
}
