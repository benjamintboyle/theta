package theta.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.domain.Stock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class StockTest {
	public static Stock buildTestStock() {
		return new Stock("CHK", 100, 15.0);
	}

	public static Stock buildTestStockShort() {
		return new Stock("CHK", -100, 15.0);
	}

	@Test
	public void quantityTest() {
		Stock stock = StockTest.buildTestStock();

		assertThat(stock.getQuantity(), is(equalTo(100)));
	}

	@Test
	public void quantityShortTest() {
		Stock stock = StockTest.buildTestStockShort();

		assertThat(stock.getQuantity(), is(equalTo(-100)));
	}

	@Test
	public void tradePriceTest() {
		Stock stock = StockTest.buildTestStockShort();

		assertThat(stock.getAverageTradePrice(), is(equalTo(15.0)));
	}

	@Test
	public void tickerTest() {
		Stock stock = StockTest.buildTestStock();

		assertThat(stock.getTicker(), is(equalTo("CHK")));
	}
}
