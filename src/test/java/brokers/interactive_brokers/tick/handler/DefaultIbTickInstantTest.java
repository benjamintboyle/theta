package brokers.interactive_brokers.tick;

import com.ib.client.TickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultIbTickInstantTest {

    private static final TickType TICK_TYPE = TickType.LAST;
    private static final double EXPECT_PRICE = 1234.56;
    private static final Instant EXPECT_TIME = LocalDate.of(2011, Month.APRIL, 11)
            .atStartOfDay(ZoneOffset.UTC).toInstant();
    private DefaultIbTickInstant sut;

    @BeforeEach
    void setup() {
        sut = new DefaultIbTickInstant(TICK_TYPE);
    }

    @Test
    void getInstantTickValues() {
        assertThat(sut.getTickType()).isEqualTo(TICK_TYPE);
    }

    @Test
    void getInstancePrice() {
        sut.updatePriceTime(EXPECT_PRICE, EXPECT_TIME);
        assertThat(sut.getInstancePrice()).isEqualTo(EXPECT_PRICE);
    }

    @Test
    void getInstantTime() {
        sut.updatePriceTime(EXPECT_PRICE, EXPECT_TIME);
        assertThat(sut.getInstantTime()).isEqualTo(EXPECT_TIME);
    }
}