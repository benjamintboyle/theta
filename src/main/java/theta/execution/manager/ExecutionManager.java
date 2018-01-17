package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.ThetaSchedulersFactory;
import theta.api.ExecutionHandler;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.domain.api.Security;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionMonitor;
import theta.execution.api.Executor;
import theta.execution.factory.ExecutableOrderFactory;

public class ExecutionManager implements Executor, ExecutionMonitor {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutionHandler executionHandler;

  private final Map<UUID, ExecutableOrder> activeOrders = new HashMap<UUID, ExecutableOrder>();

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  public ExecutionManager(ExecutionHandler executionHandler) {
    logger.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public void reverseTrade(Stock stock) {
    logger.info("Reversing Trade: {}", stock.toString());

    final Optional<ExecutableOrder> validatedOrder =
        ExecutableOrderFactory.reverseStockPosition(stock);

    validatedOrder.ifPresent(order -> executeOrder(order));
  }

  private void executeOrder(ExecutableOrder order) {
    if (addActiveTrade(order)) {
      logger.info("Executing order: {}", order);
      final Disposable disposableExecutionHandler =
          executionHandler.executeStockEquityMarketOrder(order)
              .subscribeOn(ThetaSchedulersFactory.getAsyncWaitThread()).subscribe(

                  message -> logger.info(message),

                  error -> logger.error("Order Handler encountered an error", error),

                  () -> logger.info("Order successfully filled: {}", order));

      compositeDisposable.add(disposableExecutionHandler);
    } else {
      logger.error("Order will not be executed: {}", order);
    }
  }

  private Boolean addActiveTrade(ExecutableOrder order) {
    logger.info("Adding Active Trade: {} to Execution Monitor", order);
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

    if (activeOrders.containsKey(security.getId())
        && activeOrders.get(security.getId()).getQuantity().equals(security.getQuantity())) {

      final ExecutableOrder executable = activeOrders.remove(security.getId());

      logger.info("Active Trade removed for Executable: {}", executable);

      activeTradeRemoved = Boolean.TRUE;
    }

    return activeTradeRemoved;
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    compositeDisposable.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
