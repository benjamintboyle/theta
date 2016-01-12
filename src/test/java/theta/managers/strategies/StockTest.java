package theta.managers.strategies;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.strategies.Stock;

@RunWith(MockitoJUnitRunner.class)
public class StockTest {
	public static Stock buildTestStock() {
		return new Stock("CHK", 100, 15.0, null);
	}
}
