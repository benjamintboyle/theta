package theta.domain.testutil;

import java.time.LocalDate;
import java.util.UUID;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.ticker.DefaultTicker;

public class OptionTestUtil {

  private static final LocalDate EXPIRATION = LocalDate.now().plusDays(30);

  public static Option buildTestCallOption() {
    return new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("CHK"), 1L, 15.0,
        OptionTestUtil.EXPIRATION, 0.7);
  }

  public static Option buildTestShortCallOption() {
    return new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("CHK"), -1L, 15.0,
        OptionTestUtil.EXPIRATION, 0.7);
  }

  public static Option buildTestPutOption() {
    return new Option(UUID.randomUUID(), SecurityType.PUT, DefaultTicker.from("CHK"), 1L, 15.0,
        OptionTestUtil.EXPIRATION, 0.7);
  }

  public static Option buildTestShortPutOption() {
    return new Option(UUID.randomUUID(), SecurityType.PUT, DefaultTicker.from("CHK"), -1L, 15.0,
        OptionTestUtil.EXPIRATION, 0.7);
  }

}
