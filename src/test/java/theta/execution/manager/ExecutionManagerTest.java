package theta.execution.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import theta.api.ExecutionHandler;
import theta.domain.manager.ManagerState;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;
import theta.execution.domain.CandidateStockOrder;
import theta.execution.domain.DefaultOrderStatus;
import theta.execution.domain.DefaultStockOrder;
import theta.util.MarketUtility;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionManagerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private ExecutionHandler mockExecutionHandler;
    @Mock
    private MarketUtility mockMarketUtility;

    private ExecutionManager sut;

    @BeforeEach
    void setup() {
        sut = new ExecutionManager(mockExecutionHandler, mockMarketUtility);
    }

    @Test
    void reverseTrade_whenSuccessfulOrderButNoActiveOrderStatus_thenError() {
        TestPublisher<OrderStatus> orderStatusFlux = TestPublisher.create();
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);
        when(mockExecutionHandler.executeOrder(any())).thenReturn(orderStatusFlux.flux());

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 123.45);
        CandidateStockOrder candidateStockOrder = new CandidateStockOrder(stock, ExecutionType.LIMIT, Optional.of(1.2));

        Mono<Void> tradeFinalized = sut.reverseTrade(candidateStockOrder);

        StepVerifier.create(tradeFinalized)
                .then(orderStatusFlux::complete)
                .expectError(IllegalStateException.class)
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void reverseTrade_orderFailure() {
        TestPublisher<OrderStatus> orderStatusFlux = TestPublisher.create();
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);
        when(mockExecutionHandler.executeOrder(any())).thenReturn(orderStatusFlux.flux());
        when(mockExecutionHandler.cancelOrder(any())).thenReturn(Flux.empty());

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 123.45);
        CandidateStockOrder candidateStockOrder = new CandidateStockOrder(stock, ExecutionType.LIMIT, Optional.of(1.2));

        Mono<Void> tradeFinalized = sut.reverseTrade(candidateStockOrder);

        StepVerifier.create(tradeFinalized)
                .then(() -> orderStatusFlux.error(new Throwable("Manually thrown error for testing purposes")))
                .expectError(Throwable.class)
                .verify(VERIFY_TIMEOUT);
        verify(mockExecutionHandler).cancelOrder(any());
    }

    @Test
    void convertToMarketOrderIfExists() {
        TestPublisher<OrderStatus> orderStatusFlux = TestPublisher.create();
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);
        when(mockExecutionHandler.executeOrder(any())).thenReturn(orderStatusFlux.flux());
        when(mockExecutionHandler.modifyOrder(any())).thenReturn(true);

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 123.45);
        CandidateStockOrder candidateStockOrder = new CandidateStockOrder(stock, ExecutionType.LIMIT, Optional.of(1.2));
        var providedOrderStatus = new DefaultOrderStatus(
                new DefaultStockOrder(candidateStockOrder.stock(), 200L, ExecutionAction.SELL, candidateStockOrder.executionType(), candidateStockOrder.limitPrice().orElseThrow()),
                OrderState.SUBMITTED,
                0.0,
                0,
                100L,
                0.0);
        providedOrderStatus.getOrder().setBrokerId(888);

        Mono<Void> tradeFinalized = sut.reverseTrade(candidateStockOrder);

        StepVerifier.create(tradeFinalized)
                .then(() -> orderStatusFlux.next(providedOrderStatus))
                .then(() -> sut.convertToMarketOrderIfExists(stock.getTicker()))
                .then(orderStatusFlux::complete)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void shutdown() {
        sut.shutdown();
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.STOPPING);
    }

    @Test
    void getStatus() {
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.SHUTDOWN);
    }
}
