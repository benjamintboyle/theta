package theta.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class NewYorkMarketUtilityTest {

    private NewYorkMarketUtility sut;

    @BeforeEach
    void setup() {
        sut = new NewYorkMarketUtility();
    }

    @Test
    void isDuringMarketHours_saturday() {
        Instant saturday = ZonedDateTime.of(
                LocalDate.of(2020, 10, 31),
                LocalTime.NOON,
                ZoneId.of("America/New_York"))
                .toInstant();
        assertThat(sut.isDuringMarketHours(saturday)).isFalse();
    }

    @Test
    void isDuringMarketHours_sunday() {
        Instant saturday = ZonedDateTime.of(
                LocalDate.of(2020, 11, 1),
                LocalTime.NOON,
                ZoneId.of("America/New_York"))
                .toInstant();
        assertThat(sut.isDuringMarketHours(saturday)).isFalse();
    }

    @Test
    void isDuringMarketHours_beforeOpen() {
        Instant saturday = ZonedDateTime.of(
                LocalDate.of(2020, 10, 30),
                LocalTime.of(6, 0, 0),
                ZoneId.of("America/New_York"))
                .toInstant();
        assertThat(sut.isDuringMarketHours(saturday)).isFalse();
    }

    @Test
    void isDuringMarketHours_afterOpen() {
        Instant saturday = ZonedDateTime.of(
                LocalDate.of(2020, 10, 30),
                LocalTime.of(18, 0, 0),
                ZoneId.of("America/New_York"))
                .toInstant();
        assertThat(sut.isDuringMarketHours(saturday)).isFalse();
    }

    @Test
    void isDuringMarketHours_success() {
        Instant saturday = ZonedDateTime.of(
                LocalDate.of(2020, 10, 30),
                LocalTime.NOON,
                ZoneId.of("America/New_York"))
                .toInstant();
        assertThat(sut.isDuringMarketHours(saturday)).isTrue();
    }
}
