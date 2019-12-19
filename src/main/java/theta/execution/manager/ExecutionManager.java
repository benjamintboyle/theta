package theta.execution.manager;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import theta.api.ExecutionHandler;
import theta.domain.Ticker;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionType;
import theta.execution.api.Executor;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;
import theta.execution.domain.DefaultStockOrder;
import theta.execution.factory.ExecutableOrderFactory;
import theta.util.ThetaMarketUtil;

@Slf4j
@Component
public class ExecutionManager implements Executor {

  private final ExecutionHandler executionHandler;

  private final ConcurrentMap<UUID, OrderStatus> activeOrderStatuses = new ConcurrentHashMap<>();

  private final CompositeDisposable executionManagerDisposables = new CompositeDisposable();

  private final ManagerStatus managerStatus =
      ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

  @Autowired
  public ExecutionManager(ExecutionHandler executionHandler) {
    log.info("Starting Execution Manager");
    this.executionHandler = executionHandler;
  }

  @Override
  public Completable reverseTrade(Stock stock, ExecutionType executionType,
      Optional<Double> limitPrice) {

    return Single.<ExecutableOrder>create(emitter -> {
      log.info("Reversing Trade: {}", stock);

      final Optional<ExecutableOrder> validatedOrder = ExecutableOrderFactory
          .reverseAndValidateStockPositionOrder(stock, executionType, limitPrice);

      if (validatedOrder.isPresent()) {
        emitter.onSuccess(validatedOrder.get());
      } else {
        emitter.onError(new IllegalArgumentException("Invalid order built for " + stock));
      }

    }).flatMapCompletable(this::executeOrder);
  }

  // TODO: Should probably try to remove this method; don't think it is necessary, but possibly not
  // quick fix
  @Override
  public void convertToMarketOrderIfExists(Ticker ticker) {

    activeOrderStatuses.values().stream()
        .filter(orderStatus -> orderStatus.getOrder().getTicker().equals(ticker)).forEach(

            orderStatus -> {
              final ExecutableOrder existingOrder = orderStatus.getOrder();

              if (existingOrder.getExecutionType() != ExecutionType.MARKET) {

                final ExecutableOrder modifiedToMarketOrder = new DefaultStockOrder(
                    existingOrder.getTicker(), existingOrder.getId(), existingOrder.getQuantity(),
                    existingOrder.getExecutionAction(), ExecutionType.MARKET);

                final Disposable convertToMarketDisposable =
                    executeOrder(modifiedToMarketOrder).subscribe(

                        () -> log.info("Successfully modified to Market Order: {}",
                            modifiedToMarketOrder),

                        error -> log.error("Error converting to Market Order: {}",
                            modifiedToMarketOrder, error));

                executionManagerDisposables.add(convertToMarketDisposable);
              }
            });
  }

  // May need to be converted to Maybe? Could be better design than Maybe?
  private Completable executeOrder(ExecutableOrder order) {

    return Completable.create(emitter -> {

      if (ThetaMarketUtil.isDuringNewYorkMarketHours(Instant.now())) {

        final boolean isModifiedOrder = isModifiedOrder(order);

        // New order
        if (!isModifiedOrder && !order.getBrokerId().isPresent()) {

          log.info("Executing Order {}", order);

          final Disposable disposableExecutionHandler = subscribeExecuteStockOrder(order, emitter);

          executionManagerDisposables.add(disposableExecutionHandler);
        } else if (isModifiedOrder) { // Modify existing order, if correct attributes are set

          modifyStockOrder(order);
          // TODO: Need to possibly cancel for multiple iterations; possibly convert to Maybe
        } else {
          // Something was wrong with determining if new or modified order or their parameters

          log.error("Existing order. Existing Order Status: {}. Order will not be executed: {}",
              activeOrderStatuses.get(order.getId()), order);
        }
      } else { // Market not open

        log.warn("Not during market hours. Order will not be executed: {}", order);
        // TODO: Make sure order is cancelled here
        emitter.onComplete();
      }
    });
  }

  private Disposable subscribeExecuteStockOrder(ExecutableOrder order, CompletableEmitter emitter) {
    return executionHandler.executeOrder(order).subscribe(

        orderStatus -> {
          log.info(
              "Order Status #{}: [State: {}, Commission: {}, Filled: {}, Remaining: {}, Order: {}]",
              orderStatus.getOrder().getBrokerId().orElse(null), orderStatus.getState(),
              orderStatus.getCommission(), orderStatus.getFilled(), orderStatus.getRemaining(),
              orderStatus.getOrder());
          updateActiveOrderStatus(orderStatus);
        },

        // TODO: Should probably correct cancel request
        error -> {
          log.error("Order Handler encountered an error", error);
          final Disposable disposableCancelOrder = executionHandler.cancelOrder(order).subscribe();
          executionManagerDisposables.add(disposableCancelOrder);

          log.warn("Removing order from active orders: {}", order);
          activeOrderStatuses.remove(order.getId());

          emitter.onError(error);
        },

        () -> {

          log.info("Order successfully filled: {}", order);

          if (activeOrderStatuses.remove(order.getId()) != null) {
            log.debug("Order removed from active orders list: {}", order);
          } else {
            log.warn(
                "Received filled order notification for which there is no Active Order record: {}",
                order);
          }

          emitter.onComplete();
        });
  }

  private void modifyStockOrder(ExecutableOrder order) {

    final OrderStatus activeOrderStatus = activeOrderStatuses.get(order.getId());

    if (activeOrderStatus != null && activeOrderStatus.getOrder().getBrokerId().isPresent()) {

      if (activeOrderStatus.getState() == OrderState.SUBMITTED) {

        if (!order.equals(activeOrderStatus.getOrder())) {

          log.info("Modifying order. Modified Order: {}, with current Order Status: {}", order,
              activeOrderStatus);
          executionHandler.modifyOrder(order);
        } else {
          log.warn("Modified order same as existing. Modified order: {}, Existing Order Status: {}",
              order, activeOrderStatus);
        }
      } else {
        log.warn("Attempted to modify order that is not SUBMITTED or FILLED. Modified order: {}, "
            + "existing Order Statue: {}", order, activeOrderStatus);
      }
    } else {
      log.warn("Attempted to modify order for which an existing order does not exist. Order: {}",
          order);
    }

  }

  private boolean isModifiedOrder(ExecutableOrder order) {

    boolean isModifiedOrder = false;

    final OrderStatus activeOrderStatus = activeOrderStatuses.get(order.getId());

    // No active order
    if (activeOrderStatus != null) {

      final Optional<Integer> optionalBrokerId = activeOrderStatus.getOrder().getBrokerId();

      if (optionalBrokerId.isPresent()) {

        if (activeOrderStatus.getState() != OrderState.FILLED) {
          final ExecutableOrder activeOrder = activeOrderStatus.getOrder();

          // Active order with different quantities, will be modified
          if (activeOrder.getQuantity() != order.getQuantity()) {
            order.setBrokerId(optionalBrokerId.get());
            isModifiedOrder = true;
          } else if (activeOrder.getLimitPrice().isPresent() && order.getLimitPrice().isPresent()
              && activeOrder.getLimitPrice().get() != order.getLimitPrice().get()) {
            // Active order with different Limit Prices, will be modified

            order.setBrokerId(optionalBrokerId.get());
            isModifiedOrder = true;
          } else if ((activeOrder.getExecutionType() != order.getExecutionType())) {
            // Active order with different ExecutionType, will be modified (i.e. LIMIT -> MARKET)

            order.setBrokerId(optionalBrokerId.get());
            isModifiedOrder = true;
          } else {
            // Active order and new order are the same

            log.warn("Active Order exists for {}, Active Order Status: {}, New Order Request: {}",
                order.getTicker(), activeOrderStatus, order);
          }
        } else {
          log.warn("Attempted to modify order that has filled Order Status: {}, Order: {}",
              activeOrderStatus, order);
          isModifiedOrder = true;
        }
      } else {
        log.warn("Modified order does not have Broker ID: {}, Active Order Status: {}", order,
            activeOrderStatus);
        isModifiedOrder = true;
      }
    }

    return isModifiedOrder;
  }

  private void updateActiveOrderStatus(OrderStatus orderStatus) {

    activeOrderStatuses.put(orderStatus.getOrder().getId(), orderStatus);
  }

  @Override
  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    executionManagerDisposables.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
