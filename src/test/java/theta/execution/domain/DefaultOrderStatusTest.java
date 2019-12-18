package theta.execution.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.api.OrderState;

public class DefaultOrderStatusTest {

  private DefaultOrderStatus sut = null;

  @BeforeEach
  public void setup() {
    sut = buildDefaultOrderStatus();
  }

  @Test
  public void testDefaultOrderStatus() {

    assertThat(sut.getOrder(), is(notNullValue()));
    assertThat(sut.getOrder(), is(instanceOf(ExecutableOrder.class)));
    assertThat(sut.getState(), is(OrderState.FILLED));
    assertThat(sut.getCommission(), is(1.23));
    assertThat(sut.getFilled(), is(123L));
    assertThat(sut.getRemaining(), is(0L));
    assertThat(sut.getAveragePrice(), is(123.51));
  }

  @Test
  public void testToString() {

    final String toString = sut.toString();

    assertThat("toString() should not be empty.", toString, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toString,
        not(containsString("@")));
  }

  private static DefaultOrderStatus buildDefaultOrderStatus() {
    final Stock stock = Stock.of(DefaultTicker.from("ABC"), -100L, 123.45);

    final ExecutableOrder order =
        new DefaultStockOrder(stock, 200L, ExecutionAction.BUY, ExecutionType.LIMIT, 123.5);

    return new DefaultOrderStatus(order, OrderState.FILLED, 1.23, 123, 0L, 123.51);
  }

}
