package theta.domain;

import java.time.LocalDate;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import theta.domain.api.SecurityType;

public class OptionTest {

  private static final LocalDate expiration = LocalDate.now().plusDays(30);

  public static Option buildTestCallOption() {
    return new Option(UUID.randomUUID(), SecurityType.CALL, Ticker.from("CHK"), 1L, 15.0, OptionTest.expiration, 0.7);
  }

  public static Option buildTestShortCallOption() {
    return new Option(UUID.randomUUID(), SecurityType.CALL, Ticker.from("CHK"), -1L, 15.0, OptionTest.expiration, 0.7);
  }

  public static Option buildTestPutOption() {
    return new Option(UUID.randomUUID(), SecurityType.PUT, Ticker.from("CHK"), 1L, 15.0, OptionTest.expiration, 0.7);
  }

  public static Option buildTestShortPutOption() {
    return new Option(UUID.randomUUID(), SecurityType.PUT, Ticker.from("CHK"), -1L, 15.0, OptionTest.expiration, 0.7);
  }

  @Disabled
  @Test
  public void quantityCallTest() {
    final Option call = OptionTest.buildTestCallOption();

    MatcherAssert.assertThat(call.getQuantity(), Matchers.is(Matchers.equalTo(1.0)));
  }

  @Disabled
  @Test
  public void quantityShortCallTest() {
    final Option shortCall = OptionTest.buildTestShortCallOption();

    MatcherAssert.assertThat(shortCall.getQuantity(), Matchers.is(Matchers.equalTo(-1.0)));
  }

  @Disabled
  @Test
  public void quantityPutTest() {
    final Option put = OptionTest.buildTestPutOption();

    MatcherAssert.assertThat(put.getQuantity(), Matchers.is(Matchers.equalTo(1.0)));
  }

  @Disabled
  @Test
  public void quantityShortPutTest() {
    final Option shortPut = OptionTest.buildTestShortPutOption();

    MatcherAssert.assertThat(shortPut.getQuantity(), Matchers.is(Matchers.equalTo(-1.0)));
  }

  @Test
  public void strikeCallTest() {
    final Option call = OptionTest.buildTestCallOption();

    MatcherAssert.assertThat(call.getStrikePrice(), Matchers.is(Matchers.equalTo(15.0)));
  }
}
