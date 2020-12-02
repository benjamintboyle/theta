package theta.execution.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.OrderState;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultOrderStatusTest {

    private DefaultOrderStatus sut;

    @BeforeEach
    void setup() {
        DefaultStockOrder stockOrder = new DefaultStockOrder(
                DefaultTicker.from("ABC"),
                UUID.randomUUID(),
                100L, ExecutionAction.BUY, ExecutionType.MARKET
        );
        sut = new DefaultOrderStatus(
                stockOrder,
                OrderState.SUBMITTED,
                0.33,
                10L,
                90L,
                23.34
        );
    }

    @Test
    void testOrderStatus() {
        DefaultStockOrder stockOrder = new DefaultStockOrder(
                DefaultTicker.from("ABC"),
                UUID.randomUUID(),
                100L,
                ExecutionAction.BUY,
                ExecutionType.MARKET
        );
        DefaultOrderStatus expected = new DefaultOrderStatus(
                stockOrder,
                OrderState.SUBMITTED,
                0.33,
                10L,
                90L,
                23.34
        );
        assertThat(sut).isEqualTo(expected);
    }

    @Test
    void testGetOrder() {
        DefaultStockOrder expectedOrder = new DefaultStockOrder(
                DefaultTicker.from("ABC"),
                UUID.randomUUID(),
                100L,
                ExecutionAction.BUY,
                ExecutionType.MARKET
        );
        assertThat(sut.getOrder()).isEqualTo(expectedOrder);
    }

    @Test
    void testGetState() {
        assertThat(sut.getState()).isEqualTo(OrderState.SUBMITTED);
    }

    @Test
    void testGetCommission() {
        assertThat(sut.getCommission()).isEqualTo(0.33);
    }

    @Test
    void testGetFilled() {
        assertThat(sut.getFilled()).isEqualTo(10L);
    }

    @Test
    void testGetRemaining() {
        assertThat(sut.getRemaining()).isEqualTo(90L);
    }

    @Test
    void testGetAveragePrice() {
        assertThat(sut.getAveragePrice()).isEqualTo(23.34);
    }
}
