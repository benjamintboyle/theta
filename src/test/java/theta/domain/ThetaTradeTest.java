package theta.domain;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ThetaTradeTest {
	public static ThetaTrade buildTestThetaTrade() {
		Stock stock = StockTest.buildTestStock();
		Option call = OptionTest.buildTestCallOption();
		Option put = OptionTest.buildTestPutOption();

		ThetaTrade trade = new ThetaTrade(stock);
		trade.add(call);
		trade.add(put);

		return trade;
	}
}
