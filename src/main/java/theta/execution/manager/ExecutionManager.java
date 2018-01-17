package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.ThetaSchedulersFactory;
import theta.api.ExecutionHandler;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.Executor;
import theta.execution.factory.ExecutableOrderFactory;

public class ExecutionManager implements Executor {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutionHandler executionHandler;

  private final Map<UUID, ExecutableOrder> activeOrders = new ConcurrentHashMap<>();

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

    final Optional<ExecutableOrder> validatedOrder = ExecutableOrderFactory.reverseAndValidateStockPositionOrder(stock);

    validatedOrder.ifPresent(order -> executeOrder(order));
  }

  private void executeOrder(ExecutableOrder order) {
    if (!activeOrderExists(order)) {

      logger.info("Executing order: {}", order);

      final Disposable disposableExecutionHandler = executionHandler.executeMarketStockOrder(order)
          .subscribeOn(ThetaSchedulersFactory.getAsyncWaitThread()).subscribe(

              message -> {
                logger.info(message);
              },

              // TODO: Should probably send cancel request here?
              error -> {
                logger.error("Order Handler encountered an error", error);
              },

              () -> {
                logger.info("Order successfully filled: {}", order);
                Optional<ExecutableOrder> filledOrder = Optional.ofNullable(activeOrders.remove(order.getId()));

                if (filledOrder.isPresent()) {
                  logger.info("Order removed from active orders list: {}", order);
                } else {
                  logger.warn("Received filled order notification for which there is no Active Order record: {}",
                      order);
                }
              });

      compositeDisposable.add(disposableExecutionHandler);
    } else {
      logger.error("Existing order. Order will not be executed: {}", order);
    }
  }

  private boolean activeOrderExists(ExecutableOrder order) {
    logger.info("Adding Active Trade: {} to Execution Monitor", order);

    Optional<ExecutableOrder> currentActiveOrder = Optional.ofNullable(activeOrders.get(order.getId()));

    if (!currentActiveOrder.isPresent()) {
      activeOrders.put(order.getId(), order);
    } else {
      logger.warn("Active Order exists for {}, Active Order: {}, New Order Request: ", currentActiveOrder.get(), order);
    }

    return currentActiveOrder.isPresent();
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    compositeDisposable.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
