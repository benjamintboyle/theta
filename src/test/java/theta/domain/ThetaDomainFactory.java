package theta.domain;

import java.time.LocalDate;
import theta.domain.api.SecurityType;

public class ThetaDomainFactory {

  private static final String SYMBOL = "ABC";
  private static final long STOCK_QUANTITY = -100L;
  private static final double AVERAGE_PRICE = 123.45;
  private static final double STRIKE_PRICE = 123.5;
  private static final long OPTION_QUANTITY = 1;

  public static Stock buildTestStock() {
    return Stock.of(Ticker.from(SYMBOL), STOCK_QUANTITY, AVERAGE_PRICE);
  }

  public static Option buildCallOption() {
    return buildOption(SecurityType.CALL);
  }

  public static Option buildPutOption() {
    return buildOption(SecurityType.PUT);
  }

  public static Option buildOption(SecurityType securityType) {
    return new Option(securityType, Ticker.from(SYMBOL), OPTION_QUANTITY, STRIKE_PRICE, LocalDate.now(), 1.0);
  }

  public static Theta buildTestTheta() throws Exception {

    Stock stock = buildTestStock();
    Option call = buildCallOption();
    Option put = buildPutOption();

    return Theta.of(stock, call, put).orElseThrow(() -> new Exception("Failed to generate Theta"));
  }

}
