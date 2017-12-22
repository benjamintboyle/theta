package brokers.interactive_brokers.util;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbOptionUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Optional<LocalDate> convertExpiration(String date) {
    logger.debug("Converting String: '{}' to LocalDate", date);

    Optional<LocalDate> expiration = Optional.ofNullable(null);

    if (date.length() == 8) {
      final int year = Integer.parseInt(date.substring(0, 4));
      final int month = Integer.parseInt(date.substring(4, 6));
      final int day = Integer.parseInt(date.substring(6));
      expiration = Optional.of(LocalDate.of(year, month, day));

      logger.info("Converted String: '{}' to LocalDate: {}", date, expiration.get());
    } else {
      logger.warn("Incompatible date length, expected 8 characters when converting date: {}", date);
    }

    return expiration;
  }
}
