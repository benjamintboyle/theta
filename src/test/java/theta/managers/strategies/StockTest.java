package theta.managers.strategies;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.strategies.Stock;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class StockTest {
	public static Stock buildTestStock() {
		return new Stock("CHK", 100, 15.0, null);
	}
}