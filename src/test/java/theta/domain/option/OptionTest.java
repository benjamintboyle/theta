package theta.domain.option;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import theta.domain.testutil.OptionTestUtil;

public class OptionTest {

  @Test
  public void quantityCallTest() {
    final Option call = OptionTestUtil.buildTestCallOption();

    MatcherAssert.assertThat(call.getQuantity(), Matchers.is(Matchers.equalTo(1L)));
  }

  @Test
  public void quantityShortCallTest() {
    final Option shortCall = OptionTestUtil.buildTestShortCallOption();

    MatcherAssert.assertThat(shortCall.getQuantity(), Matchers.is(Matchers.equalTo(-1L)));
  }

  @Test
  public void quantityPutTest() {
    final Option put = OptionTestUtil.buildTestPutOption();

    MatcherAssert.assertThat(put.getQuantity(), Matchers.is(Matchers.equalTo(1L)));
  }

  @Test
  public void quantityShortPutTest() {
    final Option shortPut = OptionTestUtil.buildTestShortPutOption();

    MatcherAssert.assertThat(shortPut.getQuantity(), Matchers.is(Matchers.equalTo(-1L)));
  }

  @Test
  public void strikeCallTest() {
    final Option call = OptionTestUtil.buildTestCallOption();

    MatcherAssert.assertThat(call.getStrikePrice(), Matchers.is(Matchers.equalTo(15.0)));
  }

}
