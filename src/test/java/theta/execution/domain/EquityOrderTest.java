package theta.execution.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import theta.domain.Option;
import theta.domain.OptionTest;
import theta.domain.Stock;
import theta.domain.StockTest;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;

public class EquityOrderTest {

	private final EquityOrder sutLong = new EquityOrder(100, ExecutionAction.BUY, ExecutionType.MARKET);
	private final EquityOrder sutShort = new EquityOrder(100, ExecutionAction.SELL, ExecutionType.MARKET);
	private final EquityOrder sutQuantity = new EquityOrder(101, ExecutionAction.BUY, ExecutionType.MARKET);

	@Test
	public void validateLongToShortTest() {
		Stock equity = StockTest.buildTestStockShort();

		assertThat(sutLong.validate(equity), is(equalTo(Boolean.TRUE)));
	}

	@Test
	public void validateLongToLongFailureTest() {
		Stock equity = StockTest.buildTestStock();

		assertThat(sutLong.validate(equity), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateShortToLongTest() {
		Stock equity = StockTest.buildTestStock();

		assertThat(sutShort.validate(equity), is(equalTo(Boolean.TRUE)));
	}

	@Test
	public void validateShortToShortFailureTest() {
		Stock equity = StockTest.buildTestStockShort();

		assertThat(sutShort.validate(equity), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateSecurityTypeCallFailure() {
		Option call = OptionTest.buildTestCallOption();

		assertThat(sutShort.validate(call), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateSecurityTypeShortCallFailure() {
		Option call = OptionTest.buildTestShortCallOption();

		assertThat(sutLong.validate(call), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateSecurityTypePutFailure() {
		Option put = OptionTest.buildTestPutOption();

		assertThat(sutLong.validate(put), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateSecurityTypeShortPutFailure() {
		Option put = OptionTest.buildTestShortPutOption();

		assertThat(sutShort.validate(put), is(equalTo(Boolean.FALSE)));
	}

	@Test
	public void validateQuantityFailure() {
		Stock equity = StockTest.buildTestStockShort();

		assertThat(sutQuantity.validate(equity), is(equalTo(Boolean.FALSE)));
	}
}
