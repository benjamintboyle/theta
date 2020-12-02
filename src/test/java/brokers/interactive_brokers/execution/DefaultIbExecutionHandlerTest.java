package brokers.interactive_brokers.execution;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.execution.order.IbOrderHandler;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.OrderStatus;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultIbExecutionHandlerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private Ticker mockTicker;
    @Mock
    private IbController mockIbController;
    @Mock
    private ApiController mockApiController;
    @Mock
    private ExecutableOrder mockOrder;

    private DefaultIbExecutionHandler sut;

    @BeforeEach
    void setup() {
        sut = new DefaultIbExecutionHandler(mockIbController);
    }

    @Test
    void executeOrder() {
        when(mockOrder.getSecurityType()).thenReturn(SecurityType.STOCK);
        when(mockOrder.getBrokerId()).thenReturn(Optional.of(1));
        when(mockOrder.getExecutionAction()).thenReturn(ExecutionAction.BUY);
        when(mockOrder.getExecutionType()).thenReturn(ExecutionType.MARKET);
        when(mockOrder.getTicker()).thenReturn(mockTicker);
        when(mockIbController.getController()).thenReturn(mockApiController);

        Flux<OrderStatus> orderStatusFlux = sut.executeOrder(mockOrder);

        verify(mockApiController).placeOrModifyOrder(isA(Contract.class), isA(Order.class), isA(IbOrderHandler.class));
        StepVerifier.create(orderStatusFlux)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void executeOrder_non_stock() {
        when(mockOrder.getSecurityType()).thenReturn(SecurityType.CALL);
        when(mockOrder.getBrokerId()).thenReturn(Optional.of(1));

        Flux<OrderStatus> orderStatusFlux = sut.executeOrder(mockOrder);

        verify(mockApiController, never()).placeOrModifyOrder(isA(Contract.class), isA(Order.class), isA(IbOrderHandler.class));
        StepVerifier.create(orderStatusFlux)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void modifyOrder_successful() {
        when(mockOrder.getExecutionAction()).thenReturn(ExecutionAction.BUY);
        when(mockOrder.getExecutionType()).thenReturn(ExecutionType.MARKET);
        when(mockOrder.getTicker()).thenReturn(mockTicker);
        when(mockOrder.getBrokerId()).thenReturn(Optional.of(100));
        when(mockIbController.getController()).thenReturn(mockApiController);

        boolean orderStatusFlux = sut.modifyOrder(mockOrder);

        verify(mockApiController).placeOrModifyOrder(isA(Contract.class), isA(Order.class), isA(IbOrderHandler.class));
        assertThat(orderStatusFlux).as("Modify order was successful").isTrue();
    }

    @Test
    void modifyOrder_noId() {
        when(mockOrder.getBrokerId()).thenReturn(Optional.empty());

        boolean orderStatusFlux = sut.modifyOrder(mockOrder);

        verify(mockApiController, never()).placeOrModifyOrder(isA(Contract.class), isA(Order.class), isA(IbOrderHandler.class));
        assertThat(orderStatusFlux).as("Modify order was failure").isFalse();
    }

    @Test
    void cancelOrder_successful() {
        when(mockOrder.getBrokerId()).thenReturn(Optional.of(100));
        when(mockIbController.getController()).thenReturn(mockApiController);

        Flux<OrderStatus> cancelOrderStatusFlux = sut.cancelOrder(mockOrder);

        verify(mockApiController).cancelOrder(eq(100));
        assertThat(cancelOrderStatusFlux).isEqualTo(Flux.empty());
    }

    @Test
    void cancelOrder_noId() {
        when(mockOrder.getBrokerId()).thenReturn(Optional.empty());

        Flux<OrderStatus> cancelOrderStatusFlux = sut.cancelOrder(mockOrder);

        verify(mockApiController, never()).cancelOrder(isA(Integer.class));
        assertThat(cancelOrderStatusFlux).isEqualTo(Flux.empty());
    }
}
