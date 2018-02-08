package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.api.ExecutionHandler;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.Executor;
import theta.execution.api.OrderStatus;
import theta.execution.domain.DefaultOrderStatus;
import theta.execution.domain.ExecutionType;
import theta.execution.domain.OrderState;
import theta.execution.factory.ExecutableOrderFactory;
import theta.util.ThetaMarketUtil;

public class ExecutionManager implements Executor {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutionHandler executionHandler;

  private final Map<UUID, OrderStatus> activeOrderStatuses = new ConcurrentHashMap<>();

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  public ExecutionManager(ExecutionHandler executionHandler) {
    logger.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public Completable reverseTrade(Stock stock, ExecutionType executionType, Optional<Double> limitPrice) {

    logger.info("Reversing Trade: {}", stock.toString());

    final Optional<ExecutableOrder> validatedOrder =
        ExecutableOrderFactory.reverseAndValidateStockPositionOrder(stock, executionType, limitPrice);

    Completable reverseTradeCompletable =
        Completable.error(new IllegalArgumentException("Invalid order built for " + stock));

    if (validatedOrder.isPresent()) {
      reverseTradeCompletable = executeOrder(validatedOrder.get());
    }

    return reverseTradeCompletable;
  }

  private Completable executeOrder(ExecutableOrder order) {

    return Completable.create(emitter -> {
      if (ThetaMarketUtil.isDuringMarketHours()) {

        Optional<ExecutableOrder> optionalValidOrder = newOrModifiedOrderExists(order);
        if (optionalValidOrder.isPresent() && !optionalValidOrder.get().getBrokerId().isPresent()) {

          logger.info("Executing order: {}", order);

          final Disposable disposableExecutionHandler = executionHandler.executeStockOrder(order).subscribe(

              orderStatus -> {
                logger.info("Order Status: {}, State: {}, Commission: {}, Filled: {}, Remaining: {}",
                    orderStatus.getOrder(), orderStatus.getState(), orderStatus.getCommission(),
                    orderStatus.getFilled(), orderStatus.getRemaining());
                updateActiveOrderStatus(orderStatus);
              },

              // TODO: Should probably correct cancel request
              error -> {
                logger.error("Order Handler encountered an error", error);
                Disposable disposableCancelOrder = executionHandler.cancelStockOrder(order).subscribe();
                compositeDisposable.add(disposableCancelOrder);

                logger.warn("Removing order from active orders: {}", order);
                activeOrderStatuses.remove(order.getId());

                emitter.onError(error);
              },

              () -> {
                logger.info("Order successfully filled: {}", order);
                Optional<OrderStatus> filledOrder = Optional.ofNullable(activeOrderStatuses.remove(order.getId()));

                if (filledOrder.isPresent()) {
                  logger.info("Order removed from active orders list: {}", order);
                } else {
                  logger.warn("Received filled order notification for which there is no Active Order record: {}",
                      order);
                }

                emitter.onComplete();
              });

          compositeDisposable.add(disposableExecutionHandler);
        } else if (optionalValidOrder.isPresent()) {
          // Not a new order so modify existing order
          executionHandler.modifyStockOrder(optionalValidOrder.get());
        } else {
          logger.error("Existing order. Order will not be executed: {}", order);
        }
      } else {
        logger.warn("Not during market hours. Order will not be executed: {}", order);
      }
    });
  }

  private Optional<ExecutableOrder> newOrModifiedOrderExists(ExecutableOrder order) {
    logger.info("Adding Active Trade: {} to Execution Monitor", order);

    Optional<ExecutableOrder> newOrModifiedOrder = Optional.empty();

    Optional<OrderStatus> optionalActiveOrderStatus = Optional.ofNullable(activeOrderStatuses.get(order.getId()));

    // No active order
    if (!optionalActiveOrderStatus.isPresent()) {
      updateActiveOrderStatus(order, OrderState.PENDING, -1.0, 0, order.getQuantity(), -1.0);
      newOrModifiedOrder = Optional.of(order);
    } else {

      OrderStatus activeOrderStatus = optionalActiveOrderStatus.get();
      ExecutableOrder activeOrder = activeOrderStatus.getOrder();

      // Active order with different quantities, will be modified
      if (activeOrder.getQuantity() != order.getQuantity()) {
        order.setBrokerId(activeOrderStatus.getOrder().getBrokerId().get());
        updateActiveOrderStatus(order, OrderState.PENDING, -1.0, 0, order.getQuantity(), -1.0);
        newOrModifiedOrder = Optional.of(order);
      }
      // Active order with different Limit Prices, will be modified
      else if ((activeOrder.getLimitPrice().isPresent() && order.getLimitPrice().isPresent())
          && activeOrder.getLimitPrice().get() != order.getLimitPrice().get()) {
        order.setBrokerId(activeOrderStatus.getOrder().getBrokerId().get());
        updateActiveOrderStatus(order, OrderState.PENDING, -1.0, 0, order.getQuantity(), -1.0);
        newOrModifiedOrder = Optional.of(order);
      }
      // Active order and new order are the same
      else {
        logger.warn("Active Order exists for {}, Active Order Status: {}, New Order Request: {}", order.getTicker(),
            activeOrderStatus, order);
      }
    }

    return newOrModifiedOrder;
  }

  public void updateActiveOrderStatus(ExecutableOrder executableOrder, OrderState orderState, double commission,
      long filled, long remaining, double averagePrice) {
    activeOrderStatuses.put(executableOrder.getId(),
        new DefaultOrderStatus(executableOrder, orderState, commission, filled, remaining, averagePrice));
  }

  public void updateActiveOrderStatus(OrderStatus orderStatus) {
    updateActiveOrderStatus(orderStatus.getOrder(), orderStatus.getState(), orderStatus.getCommission(),
        orderStatus.getFilled(), orderStatus.getRemaining(), orderStatus.averagePrice());
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    compositeDisposable.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
