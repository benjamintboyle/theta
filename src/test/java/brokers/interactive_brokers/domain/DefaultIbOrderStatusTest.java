package brokers.interactive_brokers.domain;

import com.ib.client.OrderStatus;
import org.junit.jupiter.api.Test;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderState;
import theta.execution.domain.DefaultOrderStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIbOrderStatusTest {

    @Test
    void testToString() {
        IbOrderStatus orderStatus = buildOrderStatus();
        assertThat(orderStatus.toString())
                .isEqualTo("Order Status: [Status: Filled, Filled: 1.0, Remaining: 2.0, " +
                        "Avg Price: 3.0, Perm Id: 4, Parent Id: 5, Last Fill Price: 6.0, Client Id: 7, " +
                        "Why Held: Important Reason]");
    }

    private IbOrderStatus buildOrderStatus() {
        DefaultIbOrderStatus.DefaultIbOrderStatusBuilder orderStatusBuilder =
                new DefaultIbOrderStatus.DefaultIbOrderStatusBuilder(OrderStatus.Filled);

        orderStatusBuilder
                .numberFilled(1.0)
                .numberRemaining(2.0)
                .withAverageFillPrice(3.0)
                .withPermId(4L)
                .withParentId(5)
                .withLastFillPrice(6.0)
                .withClientId(7)
                .withHeldReason("Important Reason");
        return orderStatusBuilder.build();
    }
}
