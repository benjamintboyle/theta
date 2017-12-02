package theta.execution.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.ExecutionHandler;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionMonitor;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.domain.EquityOrder;

public class ExecutionManager implements Executor, ExecutionMonitor {
  private static final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

  private final ExecutionHandler executionHandler;

  private final Map<UUID, Executable> activeOrders = new HashMap<UUID, Executable>();

  public ExecutionManager(ExecutionHandler executionHandler) {
    logger.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public void reverseTrade(ThetaTrade trade) {
    logger.info("Reversing Trade: {}", trade.toString());
    ExecutionAction action = null;
    if (trade.getEquity().getQuantity() > 0) {
      action = ExecutionAction.SELL;
    } else {
      action = ExecutionAction.BUY;
    }

    final Executable order = new EquityOrder(trade.getEquity(), action, ExecutionType.MARKET);
    execute(trade.getEquity(), order);
  }

  private void execute(Security security, Executable order) {
    logger.info("Executing trade of Security: {}, using Order: {}", security.toString(),
        order.toString());
    if (order.validate(security)) {
      if (addActiveTrade(order)) {
        executionHandler.executeOrder(order);
      } else {
        logger.error("Order will not be executed: {}", order);
      }
    }
  }

  private Boolean addActiveTrade(Executable order) {
    logger.info("Adding Active Trade: {} to Execution Monitor", order.toString());
    Boolean isTradeUnique = Boolean.FALSE;

    if (!activeOrders.containsKey(order.getId())) {
      activeOrders.put(order.getId(), order);
      isTradeUnique = Boolean.TRUE;
    } else {
      logger.warn("Order already placed for: {}", order);
    }

    return isTradeUnique;
  }

  @Override
  public Boolean portfolioChange(Security security) {
    logger.info("Execution Monitor was notified that Portfolio changed: {}", security.toString());
    Boolean activeTradeRemoved = Boolean.FALSE;

    if (activeOrders.containsKey(security.getId())) {
      final Executable executable = activeOrders.remove(security.getId());

      logger.info("Active Trade removed for Executable: {}", executable);

      activeTradeRemoved = Boolean.TRUE;
    } else {
      logger.debug("Existing position. Security not an active order: {}", security);
    }

    return activeTradeRemoved;
  }
}
