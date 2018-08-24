package theta.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class ThetaMarketUtilTest {

  private static final ZoneId NEW_YORK_TIMEZONE = ZoneId.of("America/New_York");
  private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
  private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(16, 00);

  @Test
  void testIsDuringMarketHoursOpenNewYork() {

    final ZonedDateTime openNewYork = ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_OPEN_TIME, NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(openNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursOpenMinusOneNewYork() {

    final ZonedDateTime openNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_OPEN_TIME.minusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(openNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursOpenPlusOneNewYork() {

    final ZonedDateTime openNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_OPEN_TIME.plusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(openNewYork), is(equalTo(true)));
  }

  @Test
  void testIsDuringMarketHoursCloseNewYork() {

    final ZonedDateTime closeNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_CLOSE_TIME, NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(closeNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursCloseMinusOneNewYork() {

    final ZonedDateTime closeNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_CLOSE_TIME.minusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(closeNewYork), is(equalTo(true)));
  }

  @Test
  void testIsDuringMarketHoursClosePlusOneNewYork() {

    final ZonedDateTime closeNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), MARKET_CLOSE_TIME.plusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(closeNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursSaturday() {

    final ZonedDateTime closeNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 14), MARKET_OPEN_TIME.plusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(closeNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursSunday() {

    final ZonedDateTime closeNewYork =
        ZonedDateTime.of(LocalDate.of(2018, 7, 15), MARKET_OPEN_TIME.plusNanos(1L), NEW_YORK_TIMEZONE);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(closeNewYork), is(equalTo(false)));
  }

  @Test
  void testIsDuringMarketHoursWithLosAngelesTimezone() {

    final ZonedDateTime openLosAngeles = ZonedDateTime.of(LocalDate.of(2018, 7, 16), LocalTime.of(6, 30).plusNanos(1L),
        ZoneId.of("America/Los_Angeles"));

    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(openLosAngeles), is(equalTo(true)));
  }

  @Test
  void testIsDuringMarketHoursWithUtcTimezone() {

    final ZonedDateTime openUtc =
        ZonedDateTime.of(LocalDate.of(2018, 7, 16), LocalTime.of(13, 30).plusNanos(1L), ZoneOffset.UTC);


    assertThat(ThetaMarketUtil.isDuringNewYorkMarketHours(openUtc), is(equalTo(true)));
  }
}
