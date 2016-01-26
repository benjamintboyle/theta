package theta.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;

@RunWith(MockitoJUnitRunner.class)
public class ThetaTradeTest {
	public static ThetaTrade buildTestThetaTrade() {
		Stock stock = StockTest.buildTestStock();
		Option call = OptionTest.buildTestShortCallOption();
		Option put = OptionTest.buildTestShortPutOption();

		ThetaTrade trade = new ThetaTrade(stock);
		trade.add(call);
		trade.add(put);

		return trade;
	}

	@Test
	public void equityThetaTradeTest() {
		ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

		assertThat(thetaTrade.getEquity(), is(equalTo(StockTest.buildTestStock())));
	}

	@Test
	public void callThetaTradeTest() {
		ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

		assertThat(thetaTrade.getCall(), is(equalTo(OptionTest.buildTestShortCallOption())));
	}

	@Test
	public void putThetaTradeTest() {
		ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

		assertThat(thetaTrade.getPut(), is(equalTo(OptionTest.buildTestShortPutOption())));
	}
}
