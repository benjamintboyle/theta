package brokers.interactive_brokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.ExecutionDomainFactory;

class IbOrderUtilTest {

  @Test
  void testBuildIbOrderNewBuyMarket() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderNewBuyMarket();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Buy Market Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Buy Market Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for New Buy Market Order", order.action(), is(Action.BUY));
    assertThat("Order Type doesn't match for New Buy Market Order", order.orderType(), is(OrderType.MKT));
  }

  @Test
  void testBuildIbOrderModifiedBuyMarket() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderModifiedBuyMarket();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Buy Market Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Buy Market Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Buy Market Order", order.action(), is(Action.BUY));
    assertThat("Order Type doesn't match for Modified Buy Market Order", order.orderType(), is(OrderType.MKT));
  }

  @Test
  void testBuildIbOrderNewSellMarket() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderNewSellMarket();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Sell Market Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Sell Market Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for New Sell Market Order", order.action(), is(Action.SELL));
    assertThat("Order Type doesn't match for New Sell Market Order", order.orderType(), is(OrderType.MKT));
  }

  @Test
  void testBuildIbOrderModifiedSellMarket() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderModifiedSellMarket();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Sell Market Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Sell Market Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Sell Market Order", order.action(), is(Action.SELL));
    assertThat("Order Type doesn't match for Modified Sell Market Order", order.orderType(), is(OrderType.MKT));
  }

  @Test
  void testBuildIbOrderNewBuyLimit() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderNewBuyLimit();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Buy Limit Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Buy Limit Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for New Buy Limit Order", order.action(), is(Action.BUY));
    assertThat("Order Type doesn't match for New Buy Limit Order", order.orderType(), is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for New Buy Limit Order", order.lmtPrice(), is(123.5));
  }

  @Test
  void testBuildIbOrderModifiedBuyLimit() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderModifiedBuyLimit();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Buy Limit Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Buy Limit Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Buy Limit Order", order.action(), is(Action.BUY));
    assertThat("Order Type doesn't match for Modified Buy Limit Order", order.orderType(), is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for Modified Buy Limit Order", order.lmtPrice(), is(123.5));
  }

  @Test
  void testBuildIbOrderNewSellLimit() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderNewSellLimit();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Sell Limit Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Sell Limit Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for New Sell Limit Order", order.action(), is(Action.SELL));
    assertThat("Order Type doesn't match for New Sell Limit Order", order.orderType(), is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for New Sell Limit Order", order.lmtPrice(), is(123.5));
  }

  @Test
  void testBuildIbOrderModifiedSellLimit() {

    ExecutableOrder executableOrder = ExecutionDomainFactory.buildTestExecutableOrderModifiedSellLimit();

    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Sell Limit Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Sell Limit Order", order.totalQuantity(), is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Sell Limit Order", order.action(), is(Action.SELL));
    assertThat("Order Type doesn't match for Modified Sell Limit Order", order.orderType(), is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for Modified Sell Limit Order", order.lmtPrice(), is(123.5));
  }
}
