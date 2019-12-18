package theta.domain.composed;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import theta.domain.testutil.OptionTestUtil;
import theta.domain.testutil.StockTestUtil;
import theta.domain.testutil.ThetaDomainTestUtil;

public class ThetaTradeTest {

  @Test
  public void stockThetaTradeTest() {
    final Theta thetaTrade = ThetaDomainTestUtil.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getStock(),
        Matchers.is(Matchers.equalTo(StockTestUtil.buildTestStock())));
  }

  @Test
  public void callThetaTradeTest() {
    final Theta thetaTrade = ThetaDomainTestUtil.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getCall(),
        Matchers.is(Matchers.equalTo(OptionTestUtil.buildTestShortCallOption())));
  }

  @Test
  public void putThetaTradeTest() {
    final Theta thetaTrade = ThetaDomainTestUtil.buildTestThetaTrade();

    MatcherAssert.assertThat(thetaTrade.getPut(),
        Matchers.is(Matchers.equalTo(OptionTestUtil.buildTestShortPutOption())));
  }

}
