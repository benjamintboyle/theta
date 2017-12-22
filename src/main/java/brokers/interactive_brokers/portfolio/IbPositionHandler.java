package brokers.interactive_brokers.portfolio;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.client.Contract;
import com.ib.controller.ApiController.IPositionHandler;
import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.util.IbOptionUtil;
import brokers.interactive_brokers.util.IbStringUtil;
import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.api.SecurityType;
import theta.portfolio.api.PortfolioObserver;

public class IbPositionHandler implements IPositionHandler, PositionHandler {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Map IB Id to Internal Id
  private final Map<Integer, UUID> contractIdMap = new HashMap<Integer, UUID>();

  private final IbController controller;
  private PortfolioObserver portfolioObserver;

  public IbPositionHandler(IbController controller) {
    logger.info("Starting Interactive Brokers Position Handler");
    this.controller = controller;
  }

  private UUID generateId(Integer contractId) {
    UUID uuid = UUID.randomUUID();

    if (contractIdMap.containsKey(contractId)) {
      uuid = contractIdMap.get(contractId);
    } else {
      contractIdMap.put(contractId, uuid);
    }

    return uuid;
  }

  @Override
  public synchronized void position(String account, Contract contract, double position, double avgCost) {
    logger.info(
        "Handler has received position from Brokers servers: Account: {}, Position: {}, Average Cost: {}, Contract: [{}]",
        account, position, avgCost, IbStringUtil.toStringContract(contract));

    switch (contract.secType()) {
      case STK:
        final Stock stock = new Stock(generateId(contract.conid()), contract.symbol(), position, avgCost);
        portfolioObserver.acceptPosition(stock);

        break;
      case OPT:
        SecurityType securityType = null;
        switch (contract.right()) {
          case Call:
            securityType = SecurityType.CALL;
            break;
          case Put:
            securityType = SecurityType.PUT;
            break;
          default:
            logger.error("Could not identify Contract Right: {}", IbStringUtil.toStringContract(contract));
            break;
        }

        final Optional<LocalDate> optionalExpiration =
            IbOptionUtil.convertExpiration(contract.lastTradeDateOrContractMonth());

        if (optionalExpiration.isPresent()) {
          final Option option = new Option(generateId(contract.conid()), securityType, contract.symbol(), position,
              contract.strike(), optionalExpiration.get(), avgCost);
          portfolioObserver.acceptPosition(option);
        } else {
          logger.error("Invalid Option Expiration for Contract: ", IbStringUtil.toStringContract(contract));
        }
        break;
      default:
        logger.error("Can not determine Position Type: {}", IbStringUtil.toStringContract(contract));
        break;
    }
  }

  @Override
  public void positionEnd() {
    logger.info("Received Position End notification");
  }

  @Override
  public void subscribePositions(PortfolioObserver observer) {
    logger.info("Portfolio Manager is observing Handler");
    portfolioObserver = observer;
  }

  @Override
  public void requestPositionsFromBrokerage() {
    logger.info("Requesting Positions from Interactive Brokers");

    controller.getController().reqPositions(this);
  }
}
