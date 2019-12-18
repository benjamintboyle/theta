package theta.domain.testutil;

import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

public class StockTestUtil {

  public static Stock buildTestStock() {
    return Stock.of(DefaultTicker.from("CHK"), 100L, 15.0);
  }

  public static Stock buildTestStockShort() {
    return Stock.of(DefaultTicker.from("CHK"), -100L, 15.0);
  }

}
