package brokers.interactivebrokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class IbOptionUtilTest {

  @Test
  public void testConvertExpiration() {

    final LocalDate expectedDate = LocalDate.of(2018, 2, 9);
    final String dateToConvert = "20180209";

    final LocalDate convertedDate = IbOptionUtil.convertExpiration(dateToConvert);

    assertThat(convertedDate, is(equalTo(expectedDate)));
  }

}
