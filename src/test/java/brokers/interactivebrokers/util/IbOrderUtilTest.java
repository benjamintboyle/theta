package brokers.interactivebrokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import org.junit.jupiter.api.Test;
import theta.domain.testutil.ExecutionDomainTestUtil;
import theta.execution.api.ExecutableOrder;

class IbOrderUtilTest {

  @Test
  public void testBuildIbOrderNewBuyMarket() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderNewBuyMarket();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Buy Market Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Buy Market Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for New Buy Market Order", order.action(),
        is(Action.BUY));
    assertThat("Order Type doesn't match for New Buy Market Order", order.orderType(),
        is(OrderType.MKT));
  }

  @Test
  public void testBuildIbOrderModifiedBuyMarket() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderModifiedBuyMarket();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Buy Market Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Buy Market Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Buy Market Order", order.action(),
        is(Action.BUY));
    assertThat("Order Type doesn't match for Modified Buy Market Order", order.orderType(),
        is(OrderType.MKT));
  }

  @Test
  public void testBuildIbOrderNewSellMarket() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderNewSellMarket();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Sell Market Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Sell Market Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for New Sell Market Order", order.action(),
        is(Action.SELL));
    assertThat("Order Type doesn't match for New Sell Market Order", order.orderType(),
        is(OrderType.MKT));
  }

  @Test
  public void testBuildIbOrderModifiedSellMarket() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderModifiedSellMarket();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Sell Market Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Sell Market Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Sell Market Order",
        order.action(), is(Action.SELL));
    assertThat("Order Type doesn't match for Modified Sell Market Order", order.orderType(),
        is(OrderType.MKT));
  }

  @Test
  public void testBuildIbOrderNewBuyLimit() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderNewBuyLimit();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Buy Limit Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Buy Limit Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for New Buy Limit Order", order.action(),
        is(Action.BUY));
    assertThat("Order Type doesn't match for New Buy Limit Order", order.orderType(),
        is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for New Buy Limit Order", order.lmtPrice(),
        is(123.5));
  }

  @Test
  public void testBuildIbOrderModifiedBuyLimit() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderModifiedBuyLimit();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Buy Limit Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Buy Limit Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Buy Limit Order", order.action(),
        is(Action.BUY));
    assertThat("Order Type doesn't match for Modified Buy Limit Order", order.orderType(),
        is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for Modified Buy Limit Order", order.lmtPrice(),
        is(123.5));
  }

  @Test
  public void testBuildIbOrderNewSellLimit() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderNewSellLimit();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for New Sell Limit Order", order.orderId(), is(0));
    assertThat("Order quantity doesn't match for New Sell Limit Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for New Sell Limit Order", order.action(),
        is(Action.SELL));
    assertThat("Order Type doesn't match for New Sell Limit Order", order.orderType(),
        is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for New Sell Limit Order", order.lmtPrice(),
        is(123.5));
  }

  @Test
  public void testBuildIbOrderModifiedSellLimit() {

    final ExecutableOrder executableOrder =
        ExecutionDomainTestUtil.buildTestExecutableOrderModifiedSellLimit();

    final Order order = IbOrderUtil.buildIbOrder(executableOrder);

    assertThat("Order Id doesn't match for Modified Sell Limit Order", order.orderId(), is(1234));
    assertThat("Order quantity doesn't match for Modified Sell Limit Order", order.totalQuantity(),
        is(200.0));
    assertThat("Order Execution Action doesn't match for Modified Sell Limit Order", order.action(),
        is(Action.SELL));
    assertThat("Order Type doesn't match for Modified Sell Limit Order", order.orderType(),
        is(OrderType.LMT));
    assertThat("Order Limit Price doesn't match for Modified Sell Limit Order", order.lmtPrice(),
        is(123.5));
  }
}
