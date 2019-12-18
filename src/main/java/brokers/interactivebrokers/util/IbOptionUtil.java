package brokers.interactivebrokers.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IbOptionUtil {

  private IbOptionUtil() {

  }

  public static LocalDate convertExpiration(String date) {
    return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
  }
}
