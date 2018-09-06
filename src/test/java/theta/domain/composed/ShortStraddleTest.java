package theta.domain.composed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.option.OptionTest;
import theta.domain.ticker.DefaultTicker;

class ShortStraddleTest {

  private final ShortStraddle sut = ShortStraddleTest.buildShortStradle();

  @Test
  void testHashCode() {

    final int expectedHashCode = Objects.hash(sut.getCall(), sut.getPut());

    assertThat(sut.hashCode(), is(equalTo(expectedHashCode)));
  }

  @Test
  void testOf() {
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    final ShortStraddle straddle = ShortStraddle.of(call, put);

    assertThat(straddle, is(notNullValue()));
    assertThat(straddle.getId(), isA(UUID.class));
    assertThat(straddle.getCall(), is(equalTo(call)));
    assertThat(straddle.getPut(), is(equalTo(put)));
  }

  @Test
  void testGetId() {
    assertThat(sut.getId(), is(notNullValue()));
    assertThat(sut.getId(), isA(UUID.class));
  }

  @Test
  void testGetSecurityType() {
    assertThat(sut.getSecurityType(), is(SecurityType.SHORT_STRADDLE));
  }

  @Test
  void testGetTicker() {
    assertThat(sut.getTicker(), is(DefaultTicker.from("CHK")));
  }

  @Test
  void testGetQuantity() {
    assertThat(sut.getQuantity(), is(1L));
  }

  @Test
  void testGetPrice() {
    assertThat(sut.getPrice(), is(15.0));
  }

  @Test
  void testGetStrikePrice() {
    assertThat(sut.getStrikePrice(), is(15.0));
  }

  @Test
  void testGetExpiration() {
    assertThat(sut.getExpiration(), is(LocalDate.now().plusDays(30)));
  }

  @Test
  void testGetCall() {
    final Option expectedCall = OptionTest.buildTestShortCallOption();

    assertThat(sut.getCall(), is(expectedCall));
  }

  @Test
  void testGetPut() {
    final Option expectedPut = OptionTest.buildTestShortPutOption();

    assertThat(sut.getPut(), is(expectedPut));
  }

  @Test
  void testToString() {
    assertThat(sut.toString(), is(notNullValue()));
  }

  @Test
  void testEqualsObject() {
    final ShortStraddle expectedStraddle = ShortStraddleTest.buildShortStradle();

    assertThat(sut, is(expectedStraddle));
  }

  public static ShortStraddle buildShortStradle() {
    final Option call = OptionTest.buildTestShortCallOption();
    final Option put = OptionTest.buildTestShortPutOption();

    return ShortStraddle.of(call, put);
  }

}
