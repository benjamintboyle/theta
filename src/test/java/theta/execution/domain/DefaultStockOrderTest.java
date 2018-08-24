package theta.execution.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import theta.domain.Stock;
import theta.domain.ThetaDomainFactory;
import theta.execution.api.ExecutableOrder;

public class DefaultStockOrderTest {

  private static int expectedBrokerId = 1234;

  @Test
  public void testDefaultStockOrder() {

    ExecutableOrder expectedOrder = ExecutionDomainFactory.buildTestExecutableOrderNewBuyLimit();

    DefaultStockOrder order = new DefaultStockOrder(ThetaDomainFactory.buildTestStock(), expectedOrder.getQuantity(),
        expectedOrder.getExecutionAction(), expectedOrder.getExecutionType(), expectedOrder.getLimitPrice().get());

    assertThat("Security Type does not match", order.getSecurityType(), is(expectedOrder.getSecurityType()));
    assertThat("Quantity does not match", order.getQuantity(), is(expectedOrder.getQuantity()));
    assertThat("Execution Action does not match", order.getExecutionAction(), is(expectedOrder.getExecutionAction()));
    assertThat("Execution Type does not match", order.getExecutionType(), is(expectedOrder.getExecutionType()));
    assertThat("Limit Price does not match", order.getLimitPrice(), is(expectedOrder.getLimitPrice()));
    assertThat("Expected Order and Order do not match", order, is(expectedOrder));
  }

  @Test
  public void testSetGetBrokerId() {

    ExecutableOrder expectedOrder = ExecutionDomainFactory.buildTestExecutableOrderNewBuyLimit();

    expectedOrder.setBrokerId(expectedBrokerId);

    assertThat("Broker Id does not match", expectedOrder.getBrokerId().get(), is(expectedBrokerId));
  }

  @Test
  public void testHashCode() {

    ExecutableOrder order = ExecutionDomainFactory.buildTestExecutableOrderNewBuyLimit();

    int expectedHash = Objects.hash(order.getTicker(), order.getQuantity(), order.getExecutionAction(),
        order.getSecurityType(), order.getSecurityType(), order.getExecutionType(), order.getLimitPrice());

    assertThat("Hash code does not match", order.hashCode(), is(expectedHash));
  }

  @Test
  public void testEqualsObject() {

    DefaultStockOrder expectedOrder = ExecutionDomainFactory.buildTestDefaultStockOrderNewBuyLimit();

    Stock stock = Stock.of(expectedOrder.getTicker(), expectedOrder.getQuantity(), 123.5);

    DefaultStockOrder order = new DefaultStockOrder(stock, expectedOrder.getQuantity(),
        expectedOrder.getExecutionAction(), expectedOrder.getExecutionType(), expectedOrder.getLimitPrice().get());

    assertThat("Expected Order and Order are not equal", order, is(equalTo(expectedOrder)));
  }

}
