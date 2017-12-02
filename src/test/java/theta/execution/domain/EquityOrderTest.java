package theta.execution.domain;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import theta.domain.Option;
import theta.domain.OptionTest;
import theta.domain.Stock;
import theta.domain.StockTest;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EquityOrderTest {

  private final EquityOrder sutLong = new EquityOrder(UUID.randomUUID(), "CHK", Double.valueOf(100),
      ExecutionAction.BUY, ExecutionType.MARKET);
  private final EquityOrder sutQuantity = new EquityOrder(UUID.randomUUID(), "CHK",
      Double.valueOf(101), ExecutionAction.BUY, ExecutionType.MARKET);
  private final EquityOrder sutShort = new EquityOrder(UUID.randomUUID(), "CHK",
      Double.valueOf(100), ExecutionAction.SELL, ExecutionType.MARKET);

  @Test
  public void validateLongToLongFailureTest() {
    final Stock equity = StockTest.buildTestStock();

    MatcherAssert.assertThat(sutLong.validate(equity),
        Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateLongToShortTest() {
    final Stock equity = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutLong.validate(equity), Matchers.is(Matchers.equalTo(Boolean.TRUE)));
  }

  @Test
  public void validateQuantityFailure() {
    final Stock equity = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutQuantity.validate(equity),
        Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeCallFailure() {
    final Option call = OptionTest.buildTestCallOption();

    MatcherAssert.assertThat(sutShort.validate(call), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypePutFailure() {
    final Option put = OptionTest.buildTestPutOption();

    MatcherAssert.assertThat(sutLong.validate(put), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeShortCallFailure() {
    final Option call = OptionTest.buildTestShortCallOption();

    MatcherAssert.assertThat(sutLong.validate(call), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateSecurityTypeShortPutFailure() {
    final Option put = OptionTest.buildTestShortPutOption();

    MatcherAssert.assertThat(sutShort.validate(put), Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }

  @Test
  public void validateShortToLongTest() {
    final Stock equity = StockTest.buildTestStock();

    MatcherAssert.assertThat(sutShort.validate(equity),
        Matchers.is(Matchers.equalTo(Boolean.TRUE)));
  }

  @Test
  public void validateShortToShortFailureTest() {
    final Stock equity = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(sutShort.validate(equity),
        Matchers.is(Matchers.equalTo(Boolean.FALSE)));
  }
}
