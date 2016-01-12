package theta.managers.strategies;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.strategies.Option;
import theta.strategies.Stock;
import theta.strategies.ThetaTrade;

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
