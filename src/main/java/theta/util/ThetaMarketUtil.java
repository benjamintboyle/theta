package theta.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ThetaMarketUtil {

  // TODO: Put into external properties file
  private static final ZoneId MARKET_TIMEZONE = ZoneId.of("America/New_York");
  private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
  private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(16, 00);

  private ThetaMarketUtil() {

  }

  /**
   * Checks if Instant passed in is during New York market hours.
   *
   * @param timeToCheck Instant to check
   * @return True if Instant passed in is during New York market hours
   */
  public static boolean isDuringNewYorkMarketHours(Instant timeToCheck) {

    final ZonedDateTime marketTimeNow = timeToCheck.atZone(MARKET_TIMEZONE);
    final DayOfWeek marketTimeNowDayOfWeek = DayOfWeek.from(marketTimeNow);
    final LocalTime marketLocalTime = marketTimeNow.toLocalTime();

    return marketTimeNowDayOfWeek != DayOfWeek.SATURDAY
        && marketTimeNowDayOfWeek != DayOfWeek.SUNDAY && marketLocalTime.isAfter(MARKET_OPEN_TIME)
        && marketLocalTime.isBefore(MARKET_CLOSE_TIME);
  }
}
