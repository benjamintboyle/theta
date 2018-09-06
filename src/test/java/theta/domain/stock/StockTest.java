package theta.domain.stock;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import theta.domain.ticker.DefaultTicker;

public class StockTest {
  public static Stock buildTestStock() {
    return Stock.of(DefaultTicker.from("CHK"), 100L, 15.0);
  }

  public static Stock buildTestStockShort() {
    return Stock.of(DefaultTicker.from("CHK"), -100L, 15.0);
  }

  @Disabled
  @Test
  public void quantityTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(stock.getQuantity(), Matchers.is(Matchers.equalTo(100.0)));
  }

  @Disabled
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

  @Disabled
  @Test
  public void tickerTest() {
    final Stock stock = StockTest.buildTestStock();

    MatcherAssert.assertThat(stock.getTicker(), Matchers.is(Matchers.equalTo("CHK")));
  }
}
