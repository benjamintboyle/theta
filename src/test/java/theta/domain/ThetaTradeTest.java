package theta.domain;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ThetaTradeTest {
  public static ThetaTrade buildTestThetaTrade() {
    final Stock stock = StockTest.buildTestStock();
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    final ThetaTrade trade = ThetaTrade.of(stock, call, put).get();

    return trade;
  }

  public static ThetaTrade buildTestShortThetaTrade() {
    final Stock stock = StockTest.buildTestStockShort();
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    final ThetaTrade trade = ThetaTrade.of(stock, call, put).get();

    return trade;
  }

  @Ignore
  @Test
  public void equityThetaTradeTest() {
    final ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getStock(), Matchers.is(Matchers.equalTo(StockTest.buildTestStock())));
  }

  @Ignore
  @Test
  public void callThetaTradeTest() {
    final ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getCall(),
        Matchers.is(Matchers.equalTo(OptionTest.buildTestShortCallOption())));
  }

  @Ignore
  @Test
  public void putThetaTradeTest() {
    final ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getPut(), Matchers.is(Matchers.equalTo(OptionTest.buildTestShortPutOption())));
  }
}
