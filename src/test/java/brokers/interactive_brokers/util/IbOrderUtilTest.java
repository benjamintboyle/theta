package brokers.interactive_brokers.util;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types;
import org.junit.jupiter.api.Test;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.domain.DefaultStockOrder;

import static org.assertj.core.api.Assertions.assertThat;

class IbOrderUtilTest {

    @Test
    void buildIbOrderTest() {
        Order actualOrder = IbOrderUtil.buildIbOrder(buildExecutableOrder());

        assertThat(actualOrder.orderId()).isEqualTo(0);
        assertThat(actualOrder.totalQuantity()).isEqualTo(99.0);
        assertThat(actualOrder.action()).isEqualTo(Types.Action.SELL);
        assertThat(actualOrder.orderType()).isEqualTo(OrderType.LMT);
        assertThat(actualOrder.lmtPrice()).isEqualTo(2.3);
    }

    private ExecutableOrder buildExecutableOrder() {
        Stock stock = Stock.of(DefaultTicker.from("DEF"), 0L, 0.0);
        return new DefaultStockOrder(stock, 99L, ExecutionAction.SELL, ExecutionType.LIMIT, 2.3);
    }
}