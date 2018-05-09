package theta.domain;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ThetaTradeTest {
  public static Theta buildTestThetaTrade() {
    final Stock stock = StockTest.buildTestStock();
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    final Theta trade = Theta.of(stock, call, put).get();

    return trade;
  }

  public static Theta buildTestShortThetaTrade() {
    final Stock stock = StockTest.buildTestStockShort();
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    final Theta trade = Theta.of(stock, call, put).get();

    return trade;
  }

  @Disabled
  @Test
  public void stockThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getStock(), Matchers.is(Matchers.equalTo(StockTest.buildTestStock())));
  }

  @Disabled
  @Test
  public void callThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getCall(),
        Matchers.is(Matchers.equalTo(OptionTest.buildTestShortCallOption())));
  }

  @Disabled
  @Test
  public void putThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getPut(), Matchers.is(Matchers.equalTo(OptionTest.buildTestShortPutOption())));
  }
}
