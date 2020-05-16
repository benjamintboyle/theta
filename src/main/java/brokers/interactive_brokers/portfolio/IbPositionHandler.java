package brokers.interactive_brokers.portfolio;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOptionUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.client.Contract;
import com.ib.controller.ApiController.IPositionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import theta.api.PositionHandler;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class IbPositionHandler implements IPositionHandler, PositionHandler {
    private static final Logger logger = LoggerFactory.getLogger(IbPositionHandler.class);

    // Map IB Id to Internal Id
    private final Map<Integer, UUID> contractIdMap = new HashMap<>();

    private final IbController controller;

    private final ReplayProcessor<Security> subjectPositions = ReplayProcessor.create();
    private final ReplayProcessor<Instant> subjectPositionEndTime = ReplayProcessor.create(1);

    private final Composite positionHandlerDisposables = Disposables.composite();

    // TODO: Move to properties file
    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(3);

    public IbPositionHandler(IbController controller) {
        logger.info("Starting Interactive Brokers Position Handler");
        this.controller = controller;
    }

    @Override
    public Flux<Security> requestPositionsFromBrokerage() {

        logger.info("Requesting Positions from Interactive Brokers");

        controller.getController().reqPositions(this);

        return getPositionEnd().next().ignoreElement().thenMany(subjectPositions.onBackpressureBuffer());
        // Don't let IB threads out of brokers.interactive_brokers package (TEMPORARILY disabled to
        // determine thread performance)
        // .observeOn(ThetaSchedulersFactory.computeThread())
    }

    @Override
    public Flux<Instant> getPositionEnd() {
        return subjectPositionEndTime.timeout(TIMEOUT_DURATION);
        // Don't let IB threads out of brokers.interactive_brokers package (TEMPORARILY disabled to
        // determine thread performance)
        // .observeOn(ThetaSchedulersFactory.ioThread())
    }

    @Override
    public void position(String account, Contract contract, double position, double avgCost) {

        logger.debug(
                "Received position from Brokers servers: Quantity: {}, "
                        + "Contract: [{}], Account: {}, Average Cost: {}",
                position, IbStringUtil.toStringContract(contract), account, avgCost);

        processIbPosition(contract, position, avgCost);
    }

    @Override
    public void positionEnd() {
        logger.info("Received Position End notification");
        subjectPositionEndTime.onNext(Instant.now());
    }

    @Override
    public void shutdown() {

        if (!subjectPositions.hasCompleted()) {
            logger.debug("Completing Position Subject");
            subjectPositions.onComplete();
        } else {
            logger.warn("Tried to complete Position Subject when it is already completed.");
        }

        if (!positionHandlerDisposables.isDisposed()) {
            logger.debug("Disposing IbPositionHandler Disposable");
            positionHandlerDisposables.dispose();
        } else {
            logger.warn("Tried to dispose of already disposed Position Handler Disposables");
        }
    }

    private void processIbPosition(Contract contract, double position, double avgCost) {

        switch (contract.secType()) {
            case STK -> {
                final Stock stock = generateStock(contract, position, avgCost);
                subjectPositions.onNext(stock);
            }
            case OPT -> {
                final Option option = generateOption(contract, position, avgCost);
                if (option != null) {
                    subjectPositions.onNext(option);
                } else {

                    logger.error("Option not processed for Contract: {}, Position: {}, Average Cost: {}",
                            IbStringUtil.toStringContract(contract), position, avgCost);
                }
            }
            default -> logger.error("Can not determine Position Type: {}", IbStringUtil.toStringContract(contract));
        }
    }

    private Stock generateStock(Contract contract, double position, double avgCost) {

        final long quantity = convertQuantityToLongCheckingIfWholeValue(position, contract);

        return Stock.of(generateId(contract.conid()), DefaultTicker.from(contract.symbol()), quantity,
                avgCost);
    }

    private Option generateOption(Contract contract, double position, double avgCost) {
        SecurityType securityType = null;
        switch (contract.right()) {
            case Call -> securityType = SecurityType.CALL;
            case Put -> securityType = SecurityType.PUT;
            default -> logger.error("Could not identify Contract Right: {}", IbStringUtil.toStringContract(contract));
        }

        final LocalDate expirationDate =
                IbOptionUtil.convertExpiration(contract.lastTradeDateOrContractMonth());

        final long quantity = convertQuantityToLongCheckingIfWholeValue(position, contract);

        Option option = null;

        if (securityType != null) {
            option = new Option(generateId(contract.conid()), securityType,
                    DefaultTicker.from(contract.symbol()), quantity, contract.strike(), expirationDate,
                    avgCost);
        }

        return option;
    }

    private long convertQuantityToLongCheckingIfWholeValue(double quantity, Contract contract) {

        long wholeQuantity = Math.round(quantity);

        if (Math.abs(quantity - wholeQuantity) > Math.ulp(quantity)) {
            wholeQuantity = (long) quantity;

            logger.warn("Security quantity not whole value. Truncating from {} to {} for {}", quantity,
                    wholeQuantity, IbStringUtil.toStringContract(contract));
        }

        return wholeQuantity;
    }

    private UUID generateId(int contractId) {
        UUID uuid = UUID.randomUUID();
        final Integer boxedContractId = contractId;

        if (contractIdMap.containsKey(boxedContractId)) {
            uuid = contractIdMap.get(boxedContractId);
        } else {
            contractIdMap.put(boxedContractId, uuid);
        }

        return uuid;
    }

}
