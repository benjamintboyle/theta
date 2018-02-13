package theta.execution.domain;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import theta.domain.Stock;
import theta.domain.Ticker;
import theta.execution.api.ExecutableOrder;

public class DefaultOrderStatusTest {

  @Test
  public void testDefaultOrderStatus() {

    Stock stock = Stock.of(Ticker.from("ABC"), -100L, 123.45);

    ExecutableOrder order = new DefaultStockOrder(stock, 200L, ExecutionAction.BUY, ExecutionType.LIMIT, 123.5);

    DefaultOrderStatus orderStatus = new DefaultOrderStatus(order, OrderState.FILLED, 1.23, 123, 0L, 123.51);

    assertThat(orderStatus.getOrder(), is(notNullValue()));
    assertThat(orderStatus.getOrder(), is(instanceOf(ExecutableOrder.class)));
    assertThat(orderStatus.getState(), is(OrderState.FILLED));
    assertThat(orderStatus.getCommission(), is(1.23));
    assertThat(orderStatus.getFilled(), is(123L));
    assertThat(orderStatus.getRemaining(), is(0L));
    assertThat(orderStatus.averagePrice(), is(123.51));
  }

}
