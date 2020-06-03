package brokers.interactive_brokers.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class IbOptionUtilTest {

    @Test
    void convertExpiration() {
        LocalDate expectDate = LocalDate.of(2024, Month.JANUARY, 1);
        String stringDate = "20240101";

        assertThat(IbOptionUtil.convertExpiration(stringDate)).isEqualTo(expectDate);
    }
}
