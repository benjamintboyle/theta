package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.ExecutionHandler;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionMonitor;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.domain.EquityOrder;

public class ExecutionManager implements Executor, ExecutionMonitor {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutionHandler executionHandler;

  private final Map<UUID, ExecutableOrder> activeOrders = new HashMap<UUID, ExecutableOrder>();

  public ExecutionManager(ExecutionHandler executionHandler) {
    logger.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public void reverseTrade(ThetaTrade trade) {

    // TODO: This should be moved to a domain ReverseTradeOrder or possibly a factory

    logger.info("Reversing Trade: {}", trade.toString());
    ExecutionAction action = null;
    if (trade.getEquity().getQuantity() > 0) {
      action = ExecutionAction.SELL;
    } else {
      action = ExecutionAction.BUY;
    }

    final ExecutableOrder order = new EquityOrder(trade.getEquity(), action, ExecutionType.MARKET);
    logger.info("Validating trade of Security: {}, using Order: {}", trade.getEquity(),
        order.toString());
    if (order.validate(trade.getEquity())) {
      executeOrder(order);
    } else {
      logger.error("Invalid order for Reverse Trade of Security: {}, using Order: {}",
          trade.getEquity(), order.toString());
    }
  }

  private void executeOrder(ExecutableOrder order) {
    logger.info("Executing order: {}", order);
    if (addActiveTrade(order)) {
      // executionHandler.executeOrder(order);
      executionHandler.executeStockEquityMarketOrder(order).onBackpressureBuffer().subscribe(
          message -> logger.info(message),
          error -> logger.error("OrderHandler encounter an error", error),
          () -> logger.info("Order successfully filled: {}", order));
    } else {
      logger.error("Order will not be executed: {}", order);
    }
  }

  private Boolean addActiveTrade(ExecutableOrder order) {
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
      final ExecutableOrder executable = activeOrders.remove(security.getId());

      logger.info("Active Trade removed for Executable: {}", executable);

      activeTradeRemoved = Boolean.TRUE;
    } else {
      logger.debug("Existing position. Security not an active order: {}", security);
    }

    return activeTradeRemoved;
  }
}
