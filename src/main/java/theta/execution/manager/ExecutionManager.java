package theta.execution.manager;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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

  private static final ZoneId MARKET_TIMEZONE = ZoneId.of("America/New_York");
  private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
  private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(4, 00);

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

    if (duringMarketHours()) {
      if (!activeOrderExists(order)) {

        logger.info("Executing order: {}", order);

        final Disposable disposableExecutionHandler = executionHandler.executeStockMarketOrder(order).subscribe(

            message -> {
              logger.info(message);
            },

            // TODO: Should probably send cancel request here?
            error -> {
              logger.error("Order Handler encountered an error", error);
              Disposable disposableCancelOrder = executionHandler.cancelStockMarketOrder(order).subscribe();
              compositeDisposable.add(disposableCancelOrder);

              logger.warn("Removing order from active orders: {}", order);
              activeOrders.remove(order.getId());
            },

            () -> {
              logger.info("Order successfully filled: {}", order);
              Optional<ExecutableOrder> filledOrder = Optional.ofNullable(activeOrders.remove(order.getId()));

              if (filledOrder.isPresent()) {
                logger.info("Order removed from active orders list: {}", order);
              } else {
                logger.warn("Received filled order notification for which there is no Active Order record: {}", order);
              }
            });

        compositeDisposable.add(disposableExecutionHandler);
      } else {
        logger.error("Existing order. Order will not be executed: {}", order);
      }
    } else {
      logger.warn("Not during market hours. Order will not be executed: {}", order);
    }
  }

  private boolean activeOrderExists(ExecutableOrder order) {
    logger.info("Adding Active Trade: {} to Execution Monitor", order);

    Optional<ExecutableOrder> currentActiveOrder = Optional.ofNullable(activeOrders.get(order.getId()));

    if (!currentActiveOrder.isPresent()) {
      activeOrders.put(order.getId(), order);
    } else {
      logger.warn("Active Order exists for {}, Active Order: {}, New Order Request: {}", order.getTicker(),
          currentActiveOrder.get(), order);
    }

    return currentActiveOrder.isPresent();
  }

  private boolean duringMarketHours() {
    ZonedDateTime marketTimeNow = ZonedDateTime.now(MARKET_TIMEZONE);

    return DayOfWeek.from(marketTimeNow) != DayOfWeek.SATURDAY && DayOfWeek.from(marketTimeNow) != DayOfWeek.SUNDAY
        && marketTimeNow.toLocalTime().isAfter(MARKET_OPEN_TIME)
        && marketTimeNow.toLocalTime().isBefore(MARKET_CLOSE_TIME);
  }

  public void shutdown() {
    getStatus().changeState(ManagerState.STOPPING);
    compositeDisposable.dispose();
  }

  public ManagerStatus getStatus() {
    return managerStatus;
  }
}
