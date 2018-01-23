package theta.execution.domain;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import theta.domain.Option;
import theta.domain.OptionTest;
import theta.domain.Stock;
import theta.domain.StockTest;
import theta.domain.Ticker;
import theta.execution.api.ExecutableOrder;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ReverseStockOrderTest {

  private final Stock longStock = Stock.of(Ticker.from("CHK"), 100.0, 10.0);
  private final Stock wrongQuantityStock = Stock.of(Ticker.from("CHK"), 101.0, 10.0);
  private final Stock shortStock = Stock.of(Ticker.from("CHK"), -100.0, 10.0);

  private final ExecutableOrder sutLong = ReverseStockOrder.reverse(longStock);
  private final ExecutableOrder sutQuantity = ReverseStockOrder.reverse(wrongQuantityStock);
  private final ExecutableOrder sutShort = ReverseStockOrder.reverse(shortStock);

  @Test
  public void validateLongToLongFailureTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(sutLong.isValid(stock), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateLongToShortTest() {
    final Stock stock = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutLong.isValid(stock), Matchers.is(Matchers.equalTo(Boolean.TRUE)));
  }

  @Test
  public void validateQuantityFailure() {
    final Stock stock = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutQuantity.isValid(stock), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeCallFailure() {
    final Option call = OptionTest.buildTestCallOption();

    MatcherAssert.assertThat(sutShort.isValid(call), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypePutFailure() {
    final Option put = OptionTest.buildTestPutOption();

    MatcherAssert.assertThat(sutLong.isValid(put), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeShortCallFailure() {
    final Option call = OptionTest.buildTestShortCallOption();

    MatcherAssert.assertThat(sutLong.isValid(call), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeShortPutFailure() {
    final Option put = OptionTest.buildTestShortPutOption();

    MatcherAssert.assertThat(sutShort.isValid(put), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateShortToLongTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(sutShort.isValid(stock), Matchers.is(Matchers.equalTo(Boolean.TRUE)));
  }

  @Test
  public void validateShortToShortFailureTest() {
    final Stock stock = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutShort.isValid(stock), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }
}
