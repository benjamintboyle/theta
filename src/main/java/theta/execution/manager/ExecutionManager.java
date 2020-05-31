package theta.execution.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.Disposable.Composite;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import theta.api.ExecutionHandler;
import theta.domain.Ticker;
import theta.domain.manager.ManagerState;
import theta.domain.manager.ManagerStatus;
import theta.domain.stock.Stock;
import theta.execution.api.*;
import theta.execution.domain.DefaultStockOrder;
import theta.execution.factory.ExecutableOrderFactory;
import theta.util.ThetaMarketUtil;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ExecutionManager implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ExecutionHandler executionHandler;

    private static final ConcurrentMap<UUID, OrderStatus> ACTIVE_ORDER_STATUSES =
            new ConcurrentHashMap<>();

    private static final Composite executionManagerDisposables = Disposables.composite();

    private final ManagerStatus managerStatus =
            ManagerStatus.of(MethodHandles.lookup().lookupClass(), ManagerState.SHUTDOWN);

    public ExecutionManager(ExecutionHandler executionHandler) {
        logger.info("Starting Execution Manager");
        this.executionHandler = executionHandler;
    }

    @Override
    public Mono<Void> reverseTrade(Stock stock, ExecutionType executionType,
                                   Optional<Double> limitPrice) {

        logger.info("Reversing Trade: {}", stock);

        return Mono.just(ExecutableOrderFactory.reverseAndValidateStockPositionOrder(stock, executionType, limitPrice))
                .flatMap(this::executeOrder);
    }

    // TODO: Should probably try to remove this method; don't think it is necessary, but possibly not quick fix
    @Override
    public void convertToMarketOrderIfExists(Ticker ticker) {

        ACTIVE_ORDER_STATUSES.values().stream()
                .filter(orderStatus -> orderStatus.getOrder().getTicker().equals(ticker)).forEach(

                orderStatus -> {
                    final ExecutableOrder existingOrder = orderStatus.getOrder();

                    if (existingOrder.getExecutionType() != ExecutionType.MARKET) {

                        final ExecutableOrder modifiedToMarketOrder = new DefaultStockOrder(
                                existingOrder.getTicker(), existingOrder.getId(), existingOrder.getQuantity(),
                                existingOrder.getExecutionAction(), ExecutionType.MARKET);

                        final Disposable convertToMarketDisposable =
                                executeOrder(modifiedToMarketOrder).subscribe(

                                        (Void) -> logger.info("Successfully modified to Market Order: {}",
                                                modifiedToMarketOrder),

                                        error -> logger.error("Error converting to Market Order: {}",
                                                modifiedToMarketOrder, error));

                        executionManagerDisposables.add(convertToMarketDisposable);
                    }
                });
    }

    // May need to be converted to Maybe? Could be better design than Maybe?
    private Mono<Void> executeOrder(ExecutableOrder order) {
        return Mono.create(emitter -> {
            if (ExecutionManager.isNowDuringMarketHoursForOrder(order)) {
                if (!isModifiedOrder(order)) {
                    // New order if (!isModifiedOrder && !order.getBrokerId().isPresent()) {
                    logger.info("Executing Order {}", order);
                    final Disposable disposableExecutionHandler = subscribeExecuteStockOrder(order, emitter);
                    executionManagerDisposables.add(disposableExecutionHandler);
                } else if (isModifiedOrder(order)) {
                    // Modify existing order,if correct attributes are set
                    modifyStockOrder(order);
                    // TODO: Need to possibly cancel for multiple iterations; possibly convert to Maybe
                } else {
                    // Something was wrong with determining if new or modified order or their parameters
                    logger.error("Existing order. Existing Order Status: {}. Order will not be executed: {}", ACTIVE_ORDER_STATUSES.get(order.getId()), order);
                }
            } else {
                // Market not open
                logger.warn("Not during market hours. Order will not be executed: {}", order);
                // TODO: Make sure order is cancelled here
                emitter.success();
            }
        });
    }

    private void handleOrderStatus(OrderStatus orderStatus) {
        logger.info("Order Status #{}: [State: {}, Commission: {}, Filled: {}, Remaining: {}, Order: {}]",
                orderStatus.getOrder().getBrokerId().orElse(null), orderStatus.getState(),
                orderStatus.getCommission(), orderStatus.getFilled(), orderStatus.getRemaining(),
                orderStatus.getOrder());
        updateActiveOrderStatus(orderStatus);
    }

    private void handleOrderErrors(Throwable error, ExecutableOrder order) {
        logger.error("Order Handler encountered an error", error);
        final Disposable disposableCancelOrder = executionHandler.cancelOrder(order).subscribe();
        executionManagerDisposables.add(disposableCancelOrder);

        logger.warn("Removing order from active orders: {}", order);
        ACTIVE_ORDER_STATUSES.remove(order.getId());
    }

    private Disposable subscribeExecuteStockOrder(ExecutableOrder order, MonoSink<Void> emitter) {
        return executionHandler.executeOrder(order).subscribe(

                this::handleOrderStatus,

                // TODO: Should probably correct cancel request
                error -> {
                    handleOrderErrors(error, order);

                    final Disposable disposableCancelOrder = executionHandler.cancelOrder(order).subscribe();
                    executionManagerDisposables.add(disposableCancelOrder);

                    logger.warn("Removing order from active orders: {}", order);
                    ACTIVE_ORDER_STATUSES.remove(order.getId());

                    emitter.error(error);
                },

                () -> {

                    logger.info("Order successfully filled: {}", order);

                    if (ACTIVE_ORDER_STATUSES.remove(order.getId()) != null) {
                        logger.debug("Order removed from active orders list: {}", order);
                    } else {
                        logger.warn(
                                "Received filled order notification for which there is no Active Order record: {}",
                                order);
                    }

                    emitter.success();
                });
    }

    private void modifyStockOrder(ExecutableOrder order) {

        final OrderStatus activeOrderStatus = ACTIVE_ORDER_STATUSES.get(order.getId());

        if (activeOrderStatus != null && activeOrderStatus.getOrder().getBrokerId().isPresent()) {

            if (activeOrderStatus.getState() == OrderState.SUBMITTED) {

                if (!order.equals(activeOrderStatus.getOrder())) {

                    logger.info("Modifying order. Modified Order: {}, with current Order Status: {}", order,
                            activeOrderStatus);
                    executionHandler.modifyOrder(order);
                } else {
                    logger.warn("Modified order same as existing. Modified order: {}, Existing Order Status: {}",
                            order, activeOrderStatus);
                }
            } else {
                logger.warn("Attempted to modify order that is not SUBMITTED or FILLED. Modified order: {}, "
                        + "existing Order Statue: {}", order, activeOrderStatus);
            }
        } else {
            logger.warn("Attempted to modify order for which an existing order does not exist. Order: {}",
                    order);
        }

    }

    private boolean isModifiedOrder(ExecutableOrder order) {

        boolean isModifiedOrder = false;

        final OrderStatus activeOrderStatus = ACTIVE_ORDER_STATUSES.get(order.getId());

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
                            && activeOrder.getLimitPrice().get().equals(order.getLimitPrice().get())) {
                        // Active order with different Limit Prices, will be modified

                        order.setBrokerId(optionalBrokerId.get());
                        isModifiedOrder = true;
                    } else if ((activeOrder.getExecutionType() != order.getExecutionType())) {
                        // Active order with different ExecutionType, will be modified (i.e. LIMIT -> MARKET)

                        order.setBrokerId(optionalBrokerId.get());
                        isModifiedOrder = true;
                    } else {
                        // Active order and new order are the same

                        logger.warn("Active Order exists for {}, Active Order Status: {}, New Order Request: {}",
                                order.getTicker(), activeOrderStatus, order);
                    }
                } else {
                    logger.warn("Attempted to modify order that has filled Order Status: {}, Order: {}",
                            activeOrderStatus, order);
                    isModifiedOrder = true;
                }
            } else {
                logger.warn("Modified order does not have Broker ID: {}, Active Order Status: {}", order,
                        activeOrderStatus);
                isModifiedOrder = true;
            }
        }

        return isModifiedOrder;
    }

    private static void updateActiveOrderStatus(OrderStatus orderStatus) {

        ACTIVE_ORDER_STATUSES.put(orderStatus.getOrder().getId(), orderStatus);
    }

    @Override
    public void shutdown() {
        getStatus().changeState(ManagerState.STOPPING);
        executionManagerDisposables.dispose();
    }

    public ManagerStatus getStatus() {
        return managerStatus;
    }

    private static boolean isNowDuringMarketHoursForOrder(ExecutableOrder order) {

        Instant now = Instant.now();
        boolean isDuringMarketHours = ThetaMarketUtil.isDuringNewYorkMarketHours(now);

        if (!isDuringMarketHours) {
            logger.warn("Now ({}) is not during market hours. Order will not be executed: {}", now, order);
        }

        return isDuringMarketHours;
    }
}
