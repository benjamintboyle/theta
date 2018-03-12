package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import theta.api.ExecutionHandler;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;
import theta.execution.factory.ExecutableOrderFactory;
import theta.util.ThetaMarketUtil;

public class ExecutionManager implements Executor {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutionHandler executionHandler;

  private final ConcurrentMap<UUID, OrderStatus> activeOrderStatuses = new ConcurrentHashMap<>();

  private final CompositeDisposable compositeDisposable = new CompositeDisposable();

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  public ExecutionManager(ExecutionHandler executionHandler) {
    logger.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public Completable reverseTrade(Stock stock, ExecutionType executionType, Optional<Double> limitPrice) {

    return Single.<ExecutableOrder>create(emitter -> {
      logger.info("Reversing Trade: {}", stock.toString());

      final Optional<ExecutableOrder> validatedOrder =
          ExecutableOrderFactory.reverseAndValidateStockPositionOrder(stock, executionType, limitPrice);

      if (validatedOrder.isPresent()) {
        emitter.onSuccess(validatedOrder.get());
      } else {
        emitter.onError(new IllegalArgumentException("Invalid order built for " + stock));
      }

    }).flatMapCompletable(order -> executeOrder(order));
  }

  // May need to be converted to Maybe? Could be better design than Maybe?
  private Completable executeOrder(ExecutableOrder order) {

    return Completable.create(emitter -> {

      if (ThetaMarketUtil.isDuringMarketHours()) {

        boolean isModifiedOrder = isModifiedOrder(order);

        // New order
        if (!isModifiedOrder && !order.getBrokerId().isPresent()) {

          logger.info("Executing Order {}", order);

          final Disposable disposableExecutionHandler = executeStockOrderWithSubscription(order, emitter);

          compositeDisposable.add(disposableExecutionHandler);
        }
        // Modify existing order, if correct attributes are set
        else if (isModifiedOrder) {

          modifyStockOrder(order);
          // TODO: Need to possibly cancel for multiple iterations; possibly convert to Maybe
        }
        // Something was wrong with determining if new or modified order or their parameters
        else {
          logger.error("Existing order. Existing Order Status: {}. Order will not be executed: {}",
              activeOrderStatuses.get(order.getId()), order);
        }
      }
      // Market not open
      else {
        logger.warn("Not during market hours. Order will not be executed: {}", order);
        // TODO: Make sure order is cancelled here
        emitter.onComplete();
      }
    });
  }

  private Disposable executeStockOrderWithSubscription(ExecutableOrder order, CompletableEmitter emitter) {
    return executionHandler.executeStockOrder(order).subscribe(

        orderStatus -> {
          logger.info("Order Status #{}: [State: {}, Commission: {}, Filled: {}, Remaining: {}, Order: {}]",
              orderStatus.getOrder().getBrokerId().orElse(null), orderStatus.getState(), orderStatus.getCommission(),
              orderStatus.getFilled(), orderStatus.getRemaining(), orderStatus.getOrder());
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

          if (activeOrderStatuses.remove(order.getId()) != null) {
            logger.debug("Order removed from active orders list: {}", order);
          } else {
            logger.warn("Received filled order notification for which there is no Active Order record: {}", order);
          }

          emitter.onComplete();
        });
  }

  private void modifyStockOrder(ExecutableOrder order) {

    OrderStatus activeOrderStatus = activeOrderStatuses.get(order.getId());

    if (activeOrderStatus != null && activeOrderStatus.getOrder().getBrokerId().isPresent()) {

      if (activeOrderStatus.getState() == OrderState.SUBMITTED) {

        if (!order.equals(activeOrderStatus.getOrder())) {

          logger.info("Modifying order. Modified Order: {}, with current Order Status: {}", order, activeOrderStatus);
          executionHandler.modifyStockOrder(order);
        } else {
          logger.warn("Modified order same as existing. Modified order: {}, Existing Order Status: {}", order,
              activeOrderStatus);
        }
      } else {
        logger.warn(
            "Attempted to modify order that is not SUBMITTED or FILLED. Modified order: {}, existing Order Statue: {}",
            order, activeOrderStatus);
      }
    } else {
      logger.warn("Attempted to modify order for which an existing order does not exist. Order: {}", order);
    }

  }

  private boolean isModifiedOrder(ExecutableOrder order) {

    boolean isModifiedOrder = false;

    OrderStatus activeOrderStatus = activeOrderStatuses.get(order.getId());

    // No active order
    if (activeOrderStatus != null) {

      if (activeOrderStatus.getOrder().getBrokerId().isPresent()) {

        if (activeOrderStatus.getState() != OrderState.FILLED) {
          ExecutableOrder activeOrder = activeOrderStatus.getOrder();

          // Active order with different quantities, will be modified
          if (activeOrder.getQuantity() != order.getQuantity()) {


            order.setBrokerId(activeOrderStatus.getOrder().getBrokerId().get());
            isModifiedOrder = true;
          }
          // Active order with different Limit Prices, will be modified
          else if ((activeOrder.getLimitPrice().isPresent() && order.getLimitPrice().isPresent())
              && activeOrder.getLimitPrice().get() != order.getLimitPrice().get()) {
            order.setBrokerId(activeOrderStatus.getOrder().getBrokerId().get());
            isModifiedOrder = true;
          }
          // Active order and new order are the same
          else {
            logger.warn("Active Order exists for {}, Active Order Status: {}, New Order Request: {}", order.getTicker(),
                activeOrderStatus, order);
          }
        } else {
          logger.warn("Attempted to modify order that has filled Order Status: {}, Order: {}", activeOrderStatus,
              order);
          isModifiedOrder = true;
        }
      } else {
        logger.warn("Modified order does not have Broker ID: {}, Active Order Status: {}", order, activeOrderStatus);
        isModifiedOrder = true;
      }
    }

    return isModifiedOrder;
  }

  private void updateActiveOrderStatus(OrderStatus orderStatus) {

    activeOrderStatuses.put(orderStatus.getOrder().getId(), orderStatus);
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    compositeDisposable.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
