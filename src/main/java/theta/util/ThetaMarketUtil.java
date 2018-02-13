package theta.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ThetaMarketUtil {

  private static final ZoneId MARKET_TIMEZONE = ZoneId.of("America/New_York");
  private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
  private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(16, 00);

  public static boolean isDuringMarketHours() {
    ZonedDateTime marketTimeNow = ZonedDateTime.now(MARKET_TIMEZONE);

    return DayOfWeek.from(marketTimeNow) != DayOfWeek.SATURDAY && DayOfWeek.from(marketTimeNow) != DayOfWeek.SUNDAY
        && marketTimeNow.toLocalTime().isAfter(MARKET_OPEN_TIME)
        && marketTimeNow.toLocalTime().isBefore(MARKET_CLOSE_TIME);
  }
}
