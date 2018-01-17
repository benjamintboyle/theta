package theta.domain;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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

  @Ignore
  @Test
  public void stockThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getStock(), Matchers.is(Matchers.equalTo(StockTest.buildTestStock())));
  }

  @Ignore
  @Test
  public void callThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getCall(),
        Matchers.is(Matchers.equalTo(OptionTest.buildTestShortCallOption())));
  }

  @Ignore
  @Test
  public void putThetaTradeTest() {
    final Theta thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getPut(), Matchers.is(Matchers.equalTo(OptionTest.buildTestShortPutOption())));
  }
}
