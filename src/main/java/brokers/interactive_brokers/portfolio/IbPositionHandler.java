package brokers.interactive_brokers.portfolio;

import static theta.util.LazyEvaluation.lazy;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Contract;
import com.ib.controller.ApiController.IPositionHandler;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOptionUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import theta.api.PositionHandler;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

public class IbPositionHandler implements IPositionHandler, PositionHandler {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Map IB Id to Internal Id
  private final Map<Integer, UUID> contractIdMap = new HashMap<>();

  private final IbController controller;

  private final Subject<Security> subjectPositions = ReplaySubject.create();
  private final Subject<Instant> subjectPositionEndTime = BehaviorSubject.create();

  private final CompositeDisposable positionHandlerDisposables = new CompositeDisposable();

  // TODO: Move to properties file
  private static final long TIMEOUT_SECONDS = 3;

  public IbPositionHandler(IbController controller) {
    logger.info("Starting Interactive Brokers Position Handler"); //$NON-NLS-1$
    this.controller = controller;
  }

  @Override
  public Flowable<Security> requestPositionsFromBrokerage() {

    logger.info("Requesting Positions from Interactive Brokers"); //$NON-NLS-1$

    controller.getController().reqPositions(this);

    return getPositionEnd().andThen(subjectPositions.toFlowable(BackpressureStrategy.BUFFER))
    // Don't let IB threads out of brokers.interactive_brokers package (TEMPORARILY disabled to
    // determine thread performance)
    // .observeOn(ThetaSchedulersFactory.computeThread())
    ;
  }

  @Override
  public Completable getPositionEnd() {
    return subjectPositionEndTime.firstOrError().ignoreElement().timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    // Don't let IB threads out of brokers.interactive_brokers package (TEMPORARILY disabled to
    // determine thread performance)
    // .observeOn(ThetaSchedulersFactory.ioThread())
    ;
  }

  @Override
  public void position(String account, Contract contract, double position, double avgCost) {

    logger.debug("Received position from Brokers servers: Quantity: {}, Contract: [{}], Account: {}, Average Cost: {}", //$NON-NLS-1$
        Double.valueOf(position), lazy(() -> IbStringUtil.toStringContract(contract)), account,
        Double.valueOf(avgCost));

    processIbPosition(contract, position, avgCost);
  }

  @Override
  public void positionEnd() {
    logger.info("Received Position End notification"); //$NON-NLS-1$
    subjectPositionEndTime.onNext(Instant.now());
  }

  @Override
  public void shutdown() {

    if (!subjectPositions.hasComplete()) {
      subjectPositions.onComplete();
    } else {
      logger.warn("Tried to complete Subject Positions when it is already completed."); //$NON-NLS-1$
    }

    if (positionHandlerDisposables.isDisposed()) {
      positionHandlerDisposables.dispose();
    } else {
      logger.warn("Tried to dispose of already disposed Position Handler Disposables"); //$NON-NLS-1$
    }
  }

  private void processIbPosition(Contract contract, double position, double avgCost) {

    switch (contract.secType()) {
      case STK:
        final Stock stock = generateStock(contract, position, avgCost);
        subjectPositions.onNext(stock);

        break;
      case OPT:
        final Option option = generateOption(contract, position, avgCost);

        if (option != null) {
          subjectPositions.onNext(option);
        } else {

          logger.error("Option not processed for Contract: {}, Position: {}, Average Cost: {}", //$NON-NLS-1$
              lazy(() -> IbStringUtil.toStringContract(contract)), Double.valueOf(position), Double.valueOf(avgCost));
        }

        break;
      case BAG:
      case BILL:
      case BOND:
      case BSK:
      case CASH:
      case CFD:
      case CMDTY:
      case FIXED:
      case FOP:
      case FUND:
      case FUT:
      case FWD:
      case ICS:
      case ICU:
      case IND:
      case IOPT:
      case NEWS:
      case None:
      case SLB:
      case WAR:
      default:

        logger.error("Can not determine Position Type: {}", lazy(() -> IbStringUtil.toStringContract(contract))); //$NON-NLS-1$
        break;
    }
  }

  private Stock generateStock(Contract contract, double position, double avgCost) {

    final long quantity = convertQuantityToLongCheckingIfWholeValue(position, contract);

    return Stock.of(generateId(contract.conid()), DefaultTicker.from(contract.symbol()), quantity, avgCost);
  }

  private Option generateOption(Contract contract, double position, double avgCost) {
    SecurityType securityType = null;
    switch (contract.right()) {
      case Call:
        securityType = SecurityType.CALL;
        break;
      case Put:
        securityType = SecurityType.PUT;
        break;
      case None:
      default:

        logger.error("Could not identify Contract Right: {}", lazy(() -> IbStringUtil.toStringContract(contract))); //$NON-NLS-1$
        break;
    }

    final LocalDate expirationDate = IbOptionUtil.convertExpiration(contract.lastTradeDateOrContractMonth());

    final long quantity = convertQuantityToLongCheckingIfWholeValue(position, contract);

    Option option = null;

    if (securityType != null) {
      option = new Option(generateId(contract.conid()), securityType, DefaultTicker.from(contract.symbol()), quantity,
          contract.strike(), expirationDate, avgCost);
    }

    return option;
  }

  private long convertQuantityToLongCheckingIfWholeValue(double quantity, Contract contract) {

    long wholeQuantity = Math.round(quantity);

    if (Math.abs(quantity - wholeQuantity) > Math.ulp(quantity)) {
      wholeQuantity = (long) quantity;

      logger.warn("Security quantity not whole value. Truncating from {} to {} for {}", quantity, wholeQuantity, //$NON-NLS-1$
          lazy(() -> IbStringUtil.toStringContract(contract)));
    }

    return wholeQuantity;
  }

  private UUID generateId(int contractId) {
    UUID uuid = UUID.randomUUID();
    final Integer boxedContractId = Integer.valueOf(contractId);

    if (contractIdMap.containsKey(boxedContractId)) {
      uuid = contractIdMap.get(boxedContractId);
    } else {
      contractIdMap.put(boxedContractId, uuid);
    }

    return uuid;
  }

}
