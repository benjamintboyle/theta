package theta.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import theta.domain.api.SecurityType;

class ShortStraddleTest {

  private ShortStraddle sut = ShortStraddleTest.buildShortStradle();

  @Test
  void testHashCode() {

    int expectedHashCode = Objects.hash(sut.getCall(), sut.getPut());

    assertThat(sut.hashCode(), is(equalTo(expectedHashCode)));
  }

  @Test
  void testOf() {
    Option call = OptionTest.buildTestShortCallOption();
    Option put = OptionTest.buildTestShortPutOption();

    ShortStraddle straddle = ShortStraddle.of(call, put);

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
    assertThat(sut.getTicker(), is(Ticker.from("CHK")));
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
    Option expectedCall = OptionTest.buildTestShortCallOption();

    assertThat(sut.getCall(), is(expectedCall));
  }

  @Test
  void testGetPut() {
    Option expectedPut = OptionTest.buildTestShortPutOption();

    assertThat(sut.getPut(), is(expectedPut));
  }

  @Test
  void testToString() {
    assertThat(sut.toString(), is(notNullValue()));
  }

  @Test
  void testEqualsObject() {
    ShortStraddle expectedStraddle = ShortStraddleTest.buildShortStradle();

    assertThat(sut, is(expectedStraddle));
  }

  public static ShortStraddle buildShortStradle() {
    Option call = OptionTest.buildTestShortCallOption();
    Option put = OptionTest.buildTestShortPutOption();

    return ShortStraddle.of(call, put);
  }

}
