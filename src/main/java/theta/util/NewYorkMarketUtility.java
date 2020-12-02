package theta.util;

import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class NewYorkMarketUtility implements MarketUtility {
    // TODO: Put into external properties file
    private static final ZoneId MARKET_TIMEZONE = ZoneId.of("America/New_York");
    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 30);
    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(16, 0);

    @Override
    public boolean isDuringMarketHours(Instant timeToCheck) {
        ZonedDateTime marketInstant = timeToCheck.atZone(MARKET_TIMEZONE);
        DayOfWeek marketDayOfWeek = DayOfWeek.from(marketInstant);
        LocalTime marketLocalTime = marketInstant.toLocalTime();

        return marketDayOfWeek != DayOfWeek.SATURDAY
                && marketDayOfWeek != DayOfWeek.SUNDAY
                && marketLocalTime.isAfter(MARKET_OPEN_TIME)
                && marketLocalTime.isBefore(MARKET_CLOSE_TIME);
    }
}
