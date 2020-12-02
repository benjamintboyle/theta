package theta.portfolio.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
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
import theta.portfolio.factory.ThetaTradeFactory;
import theta.tick.api.TickMonitor;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DefaultPortfolioManager implements PortfolioManger {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PositionHandler positionHandler;
    private final TickMonitor tickMonitor;

    // Currently active theta trades
    private final Map<UUID, Theta> thetaIdMap = new ConcurrentHashMap<>();
    // Securities to theta trade map
    private final Map<UUID, Set<UUID>> securityThetaLink = new ConcurrentHashMap<>();
    // Internal Id to Security map
    private final Map<UUID, Security> securityIdMap = new ConcurrentHashMap<>();

    private final ManagerStatus managerStatus =
            ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

    private final Composite portfolioDisposables = Disposables.composite();

    /**
     * Create Portfolio Manager with PositionHandler and TickMonitor.
     *
     * @param positionHandler Handler to maintain additions/subtractions to positions.
     * @param tickMonitor     Monitor to determine if action should be taken for tick.
     */
    public DefaultPortfolioManager(PositionHandler positionHandler, TickMonitor tickMonitor) {
        managerStatus.changeState(ManagerState.STARTING);
        this.positionHandler = positionHandler;
        this.tickMonitor = tickMonitor;
    }

    @Override
    public void shutdown() {
        positionHandler.shutdown();
        portfolioDisposables.dispose();
        if (managerStatus.getState() != ManagerState.SHUTDOWN) {
            managerStatus.changeState(ManagerState.STOPPING);
        }
    }

    public ManagerStatus getStatus() {
        return managerStatus;
    }

    /**
     * Starts monitoring of positions.
     *
     * @return Completable to functionally process positions.
     */
    public Mono<Void> startPositionProcessing() {
        logger.debug("Starting Position Processing");

        return Mono.create(emitter -> {
            final Disposable positionLoggerDisposable = positionHandler.requestPositionsFromBrokerage()
                    .map(this::processSecurity).doOnSubscribe(
                            subscription -> managerStatus.changeState(ManagerState.RUNNING))
                    .subscribe(
                            security -> PositionLogger.logPositions(thetaIdMap, securityThetaLink, securityIdMap),
                            exception -> {
                                logger.error("Issue with Processing Received Positions from Brokerage", exception);
                                emitter.error(exception);
                            },
                            () -> {
                                managerStatus.changeState(ManagerState.SHUTDOWN);
                                emitter.success();
                            });
            portfolioDisposables.add(positionLoggerDisposable);
        });
    }

    private Security processSecurity(Security security) {
        logger.info("Processing Position: {}", security);

        removePositionIfExists(security);

        if (security.getQuantity() != 0) {
            securityIdMap.put(security.getId(), security);
            processPosition(security.getTicker());
        } else {
            securityIdMap.remove(security.getId());
            logger.info("Security not processed due to 0 quantity: {}", security);
        }

        return security;
    }

    // Removes positions if security is contained within it
    private List<PriceLevel> removePositionIfExists(Security security) {
        final List<PriceLevel> removedPriceLevels = new ArrayList<>();

        final Set<UUID> thetaIds =
                Optional.ofNullable(securityThetaLink.remove(security.getId())).orElse(Set.of());

        for (final UUID thetaId : thetaIds) {
            final Optional<Theta> optionalTheta = Optional.ofNullable(thetaIdMap.remove(thetaId));

            optionalTheta.map(DefaultPriceLevel::of).ifPresent(priceLevel -> {

                removedPriceLevels.add(priceLevel);

                final Theta theta = optionalTheta.get();

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
        final List<Stock> unallocatedStocks = getUnallocatedSecuritiesOf(ticker, SecurityType.STOCK)
                .stream().map(stock -> (Stock) stock).collect(Collectors.toList());
        final List<Option> unallocatedCalls = getUnallocatedSecuritiesOf(ticker, SecurityType.CALL)
                .stream().map(call -> (Option) call).collect(Collectors.toList());
        final List<Option> unallocatedPuts = getUnallocatedSecuritiesOf(ticker, SecurityType.PUT)
                .stream().map(put -> (Option) put).collect(Collectors.toList());

        if (!unallocatedStocks.isEmpty() && !unallocatedCalls.isEmpty() && !unallocatedPuts.isEmpty()) {
            ThetaTradeFactory.processThetaTrade(unallocatedStocks, unallocatedCalls, unallocatedPuts)
                    .stream().map(this::updateSecurityMaps).distinct()
                    .forEach(tickMonitor::addMonitor);
        }
    }

    private List<Security> getUnallocatedSecuritiesOf(Ticker ticker, SecurityType securityType) {
        final List<Security> unallocatedSecurities = new ArrayList<>();

        final Set<Security> allIdsOfTickerAndSecurityType = securityIdMap.values().stream()
                .filter(otherSecurity -> otherSecurity.getTicker().equals(ticker))
                .filter(otherSecurity -> otherSecurity.getSecurityType().equals(securityType))
                .filter(otherSecurity -> otherSecurity.getQuantity() != 0).collect(Collectors.toSet());

        final Map<UUID, Long> allocatedCountMap = thetaIdMap.values().stream()
                .filter(theta -> theta.getTicker().equals(ticker))
                .map(theta -> theta.getSecurityOfType(securityType)).collect(
                        Collectors.groupingBy(Security::getId, Collectors.summingLong(Security::getQuantity)));

        for (final Security security : allIdsOfTickerAndSecurityType) {

            final long unallocatedQuantity =
                    Math.abs(security.getQuantity() - allocatedCountMap.getOrDefault(security.getId(), 0L));
            logger.debug("Calculated {} unallocated securities for {}", unallocatedQuantity, security);

            final Optional<Security> securityWithAdjustedQuantity =
                    SecurityUtil.getSecurityWithQuantity(security, unallocatedQuantity);

            securityWithAdjustedQuantity.ifPresent(unallocatedSecurities::add);
        }

        return unallocatedSecurities;
    }

    private Theta updateSecurityMaps(Theta theta) {
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

        return theta;
    }

}
