package theta.domain;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class StockTest {
  public static Stock buildTestStock() {
    return Stock.of(Ticker.from("CHK"), Double.valueOf(100), 15.0);
  }

  public static Stock buildTestStockShort() {
    return Stock.of(Ticker.from("CHK"), Double.valueOf(-100), 15.0);
  }

  @Test
  public void quantityTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(stock.getQuantity(), Matchers.is(Matchers.equalTo(100.0)));
  }

  @Test
  public void quantityShortTest() {
    final Stock stock = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(stock.getQuantity(), Matchers.is(Matchers.equalTo(-100.0)));
  }

  @Test
  public void tradePriceTest() {
    final Stock stock = StockTest.buildTestStockShort();

    MatcherAssert.assertThat(stock.getPrice(), Matchers.is(Matchers.equalTo(15.0)));
  }

  @Test
  public void tickerTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(stock.getTicker(), Matchers.is(Matchers.equalTo("CHK")));
  }
}
