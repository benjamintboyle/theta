package theta.util;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThetaMarketUtil {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final ZoneId MARKET_TIMEZONE = ZoneId.of("America/New_York");
  private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
  private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(16, 00);

  public static boolean isDuringMarketHours() {
    ZonedDateTime marketTimeNow = ZonedDateTime.now(MARKET_TIMEZONE);

    boolean duringMarketHours = DayOfWeek.from(marketTimeNow) != DayOfWeek.SATURDAY
        && DayOfWeek.from(marketTimeNow) != DayOfWeek.SUNDAY && marketTimeNow.toLocalTime().isAfter(MARKET_OPEN_TIME)
        && marketTimeNow.toLocalTime().isBefore(MARKET_CLOSE_TIME);

    if (!duringMarketHours) {
      logger.warn("Outside market hours. Current: {}, Market Hours: {}-{} M-F", marketTimeNow, MARKET_OPEN_TIME,
          MARKET_CLOSE_TIME);
    }

    return duringMarketHours;
  }
}
