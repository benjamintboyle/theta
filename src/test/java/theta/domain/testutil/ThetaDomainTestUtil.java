package theta.domain.testutil;

import java.time.LocalDate;
import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.SecurityType;
import theta.domain.composed.Theta;
import theta.domain.option.Option;
import theta.domain.pricelevel.DefaultPriceLevel;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

public class ThetaDomainTestUtil {

  private static final String SYMBOL = "ABC";
  private static final long STOCK_QUANTITY = -100L;
  private static final double AVERAGE_PRICE = 123.45;
  private static final double STRIKE_PRICE = 123.5;
  private static final long OPTION_QUANTITY = 1;

  public static Stock buildTestStock() {
    return Stock.of(DefaultTicker.from(SYMBOL), STOCK_QUANTITY, AVERAGE_PRICE);
  }

  public static Option buildCallOption() {
    return buildOption(SecurityType.CALL);
  }

  public static Option buildPutOption() {
    return buildOption(SecurityType.PUT);
  }

  public static Option buildOption(SecurityType securityType) {
    return new Option(securityType, DefaultTicker.from(SYMBOL), OPTION_QUANTITY, STRIKE_PRICE,
        LocalDate.now(), 1.0);
  }

  /**
   * Utility method which builds a generic Theta trade.
   *
   * @return Generic Theta object
   */
  public static Theta buildTestTheta() {

    final Stock stock = buildTestStock();
    final Option call = buildCallOption();
    final Option put = buildPutOption();

    return Theta.of(stock, call, put).get();
  }

  /**
   * Utility method which builds a generic Theta trade.
   *
   * @return Generic Theta object
   */
  public static Theta buildTestThetaTrade() {
    final Stock stock = StockTestUtil.buildTestStock();
    final Option call = OptionTestUtil.buildTestShortCallOption();
    final Option put = OptionTestUtil.buildTestShortPutOption();

    final Theta trade = Theta.of(stock, call, put).get();

    return trade;
  }

  public static PriceLevel buildDefaultPriceLevel() {
    return DefaultPriceLevel.from(DefaultTicker.from(SYMBOL), STRIKE_PRICE,
        PriceLevelDirection.RISES_ABOVE);
  }

}
