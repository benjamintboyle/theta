package theta.execution.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.util.Objects;
import org.junit.Test;
import theta.domain.Stock;
import theta.domain.Ticker;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;

public class DefaultStockOrderTest {

  @Test
  public void testDefaultStockOrder() {

    Stock stock = Stock.of(Ticker.from("ABC"), -100L, 123.45);

    ExecutableOrder expectedOrder = new DefaultStockOrder(stock, 200L, ExecutionAction.BUY, ExecutionType.LIMIT, 123.5);

    DefaultStockOrder order = new DefaultStockOrder(stock, expectedOrder.getQuantity(),
        expectedOrder.getExecutionAction(), expectedOrder.getExecutionType(), expectedOrder.getLimitPrice().get());

    assertThat(order.getId(), is(expectedOrder.getId()));

    assertThat(order.getSecurityType(), is(expectedOrder.getSecurityType()));
    assertThat(order.getQuantity(), is(expectedOrder.getQuantity()));
    assertThat(order.getExecutionAction(), is(expectedOrder.getExecutionAction()));
    assertThat(order.getExecutionType(), is(expectedOrder.getExecutionType()));
    assertThat(order.getLimitPrice(), is(expectedOrder.getLimitPrice()));
    assertThat(order, is(expectedOrder));
  }

  @Test
  public void testSetGetBrokerId() {

    Stock stock = Stock.of(Ticker.from("ABC"), -100L, 123.45);

    ExecutableOrder expectedOrder = new DefaultStockOrder(stock, 200L, ExecutionAction.BUY, ExecutionType.LIMIT, 123.5);

    expectedOrder.setBrokerId(1234);

    assertThat(expectedOrder.getBrokerId().get(), is(1234));
  }

  @Test
  public void testHashCode() {
    Stock stock = Stock.of(Ticker.from("ABC"), -100L, 123.45);

    ExecutableOrder order = new DefaultStockOrder(stock, 200L, ExecutionAction.BUY, ExecutionType.LIMIT, 123.5);

    int expectedHash = Objects.hash(order.getTicker(), order.getQuantity(), order.getExecutionAction(),
        order.getSecurityType(), order.getSecurityType(), order.getExecutionType(), order.getLimitPrice());

    assertThat(order.hashCode(), is(expectedHash));
  }

  @Test
  public void testEqualsObject() {

    final double averagePrice = 123.45;
    final long quantity = 200L;
    final ExecutionAction action = ExecutionAction.BUY;
    final ExecutionType execuctionType = ExecutionType.LIMIT;
    final double limitPrice = 123.5;

    Stock expectedStock = Stock.of(Ticker.from("ABC"), -100L, averagePrice);

    ExecutableOrder expectedOrder = new DefaultStockOrder(expectedStock, quantity, action, execuctionType, limitPrice);

    Stock stock = Stock.of(expectedOrder.getTicker(), expectedOrder.getQuantity(), averagePrice);

    ExecutableOrder order = new DefaultStockOrder(stock, expectedOrder.getQuantity(),
        expectedOrder.getExecutionAction(), expectedOrder.getExecutionType(), expectedOrder.getLimitPrice().get());

    assertThat(order, is(equalTo(expectedOrder)));
  }

}
