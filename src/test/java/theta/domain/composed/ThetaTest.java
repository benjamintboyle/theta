package theta.domain.composed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThetaTest {

    private static final Ticker TICKER = DefaultTicker.from("ABC");
    private static final Stock STOCK = Stock.of(TICKER, 100L, 44.44);
    private static final Option CALL = new Option(
            UUID.randomUUID(),
            SecurityType.CALL,
            TICKER,
            -1L,
            44.0,
            LocalDate.of(2020, 10, 30),
            1.22);
    private static final Option PUT = new Option(
            UUID.randomUUID(),
            SecurityType.PUT,
            TICKER,
            -1L,
            44.0,
            LocalDate.of(2020, 10, 30),
            1.22);

    private Theta sut;

    @BeforeEach
    void setup() {
        sut = Theta.of(STOCK, CALL, PUT);
    }

    @Test
    void of_builtWithShortStraddle() {
        ShortStraddle shortStraddle = ShortStraddle.of(CALL, PUT);
        Theta thetaShortStraddle = Theta.of(STOCK, shortStraddle);

        assertThat(thetaShortStraddle).isEqualTo(sut);
    }

    @Test
    void getId() {
        assertThat(sut.getId()).isInstanceOf(UUID.class);
    }

    @Test
    void getQuantity() {
        assertThat(sut.getQuantity()).isEqualTo(1L);
    }

    @Test
    void getPrice() {
        assertThat(sut.getPrice()).isEqualTo(44.0);
    }

    @Test
    void getStock() {
        assertThat(sut.getStock()).isEqualTo(STOCK);
    }

    @Test
    void getStraddle() {
        assertThat(sut.getStraddle()).isEqualTo(ShortStraddle.of(CALL, PUT));
    }

    @Test
    void getCall() {
        assertThat(sut.getCall()).isEqualTo(CALL);
    }

    @Test
    void getPut() {
        assertThat(sut.getPut()).isEqualTo(PUT);
    }

    @Test
    void getSecurityType() {
        assertThat(sut.getSecurityType()).isEqualTo(SecurityType.THETA);
    }

    @Test
    void getSecurityOfType_stock() {
        assertThat(sut.getSecurityOfType(SecurityType.STOCK)).isEqualTo(STOCK);
    }

    @Test
    void getSecurityOfType_call() {
        assertThat(sut.getSecurityOfType(SecurityType.CALL)).isEqualTo(CALL);
    }

    @Test
    void getSecurityOfType_put() {
        assertThat(sut.getSecurityOfType(SecurityType.PUT)).isEqualTo(PUT);
    }

    @Test
    void getTicker() {
        assertThat(sut.getTicker()).isEqualTo(TICKER);
    }
}
