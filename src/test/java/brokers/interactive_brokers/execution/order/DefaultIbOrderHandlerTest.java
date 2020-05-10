package brokers.interactive_brokers.execution.order;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultIbOrderHandlerTest {

    @Mock
    Appender<ILoggingEvent> appender;
    @Captor
    ArgumentCaptor<ILoggingEvent> captor;
    @Mock
    private ExecutableOrder order;

    private DefaultIbOrderHandler sut;

    @BeforeEach
    void setup() {
        sut = new DefaultIbOrderHandler(order);
    }

    @Test
    void getOrderStatus_whenNoInitialization_EmptyFlux() {
        Flux<OrderStatus> orderStatus = sut.getOrderStatus();
        StepVerifier.create(orderStatus).expectSubscription();
    }

    @Test
    void orderState() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        // Using reflection to instantiate no-args constructor as it is package default
        Constructor<?> constructor = com.ib.client.OrderState.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        com.ib.client.OrderState orderState = (com.ib.client.OrderState) constructor.newInstance();

        orderState.status("ApiPending");
        orderState.commission(1111.0);
        sut.orderState(orderState);

        sut.orderStatus(com.ib.client.OrderStatus.get("Filled"), 123, 21, 23.23,
                31L, 10, 2.1, 1, "Good reason");

        Flux<OrderStatus> orderStatusFlux = sut.getOrderStatus();

        StepVerifier.create(orderStatusFlux)
                .expectNextMatches(status -> status.getCommission() == 1111.0)
                .expectComplete()
                .verify();
    }

    @Test
    void orderStatus() {
        sut.orderStatus(com.ib.client.OrderStatus.get("ApiPending"), 123, 21, 23.23,
                31L, 10, 2.1, 1, "Good reason");
        sut.orderStatus(com.ib.client.OrderStatus.get("Filled"), 123, 21, 23.23,
                31L, 10, 2.1, 1, "Good reason");

        Flux<OrderStatus> orderStatusFlux = sut.getOrderStatus();

        StepVerifier.create(orderStatusFlux)
                .expectNextMatches(status -> status.getState().equals(OrderState.PENDING))
                .expectNextMatches(status -> status.getState().equals(OrderState.FILLED))
                .expectComplete()
                .verify();
    }

    @Test
    void handle() {
        when(order.toString()).thenReturn("Order: []");

        setupLoggingAppender();

        sut.handle(1111, "TEST MESSAGE");

        validateLogging("Order Handler Error, Error Code: 1111, Message: TEST MESSAGE for Order: " + order);
    }

    @Test
    void testToString() {
        when(order.toString()).thenReturn("Order: []");
        String expect = "Order: Order: [], Order Status: ApiPending, Commission: 0.0, Filled: 0.0, Remaining: 0.0, Average Price: 0.0";

        assertThat(sut.toString()).isEqualTo(expect);
    }

    private void setupLoggingAppender() {
        // Add appender to pull in log message
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    private void validateLogging(String message) {
        verify(appender).doAppend(captor.capture());
        assertThat(captor.getValue().getFormattedMessage())
                .isEqualTo(message);
    }
}
