package theta.domain.stock;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import theta.domain.testutil.StockTestUtil;

public class StockTest {

  @Test
  public void quantityTest() {
    final Stock stock = StockTestUtil.buildTestStock();

    MatcherAssert.assertThat(stock.getQuantity(), Matchers.is(Matchers.equalTo(100L)));
  }

  @Test
  public void quantityShortTest() {
    final Stock stock = StockTestUtil.buildTestStockShort();

    MatcherAssert.assertThat(stock.getQuantity(), Matchers.is(Matchers.equalTo(-100L)));
  }

  @Test
  public void tradePriceTest() {
    final Stock stock = StockTestUtil.buildTestStockShort();

    MatcherAssert.assertThat(stock.getPrice(), Matchers.is(Matchers.equalTo(15.0)));
  }

  @Test
  public void tickerTest() {
    final Stock stock = StockTestUtil.buildTestStock();

    MatcherAssert.assertThat(stock.getTicker().getSymbol(), Matchers.is(Matchers.equalTo("CHK")));
  }
}
