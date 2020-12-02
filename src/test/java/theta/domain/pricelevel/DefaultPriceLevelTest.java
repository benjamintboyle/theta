package theta.domain.pricelevel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.composed.Theta;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPriceLevelTest {

    private static final Ticker TICKER = DefaultTicker.from("ABC");
    private static final double PRICE = 123.5;
    private static final PriceLevelDirection PRICE_LEVEL_DIRECTION = PriceLevelDirection.FALLS_BELOW;

    private PriceLevel sut;

    @BeforeEach
    void setUp() {
        sut = DefaultPriceLevel.from(TICKER, PRICE, PRICE_LEVEL_DIRECTION);
    }

    @Test
    void ofTheta() {
        Stock stock = Stock.of(TICKER, -100L, 123.45);
        Option call = new Option(UUID.randomUUID(), SecurityType.CALL, TICKER, -1L, PRICE, LocalDate.of(2012, 10, 22), 7.89);
        Option put = new Option(UUID.randomUUID(), SecurityType.PUT, TICKER, -1L, PRICE, LocalDate.of(2012, 10, 22), 7.89);
        Theta theta = Theta.of(stock, call, put);

        assertThat(DefaultPriceLevel.of(theta)).isNotEqualTo(sut);
    }

    @Test
    void getTicker() {
        assertThat(sut.getTicker()).isEqualTo(TICKER);
    }

    @Test
    void getPrice() {
        assertThat(sut.getPrice()).isEqualTo(PRICE);
    }

    @Test
    void tradeIf() {
        assertThat(sut.tradeIf()).isEqualTo(PRICE_LEVEL_DIRECTION);
    }

    @Test
    void compareTo_differentTicker() {
        PriceLevel differentTickerPriceLevel = DefaultPriceLevel.from(DefaultTicker.from("XYZ"), PRICE, PRICE_LEVEL_DIRECTION);
        assertThat(sut.compareTo(differentTickerPriceLevel))
                .isNegative();
    }

    @Test
    void compareTo_differentPrice() {
        PriceLevel differentTickerPriceLevel = DefaultPriceLevel.from(TICKER, 1.0, PRICE_LEVEL_DIRECTION);
        assertThat(sut.compareTo(differentTickerPriceLevel))
                .isPositive();
    }

    @Test
    void compareTo_differentPriceLevel() {
        PriceLevel differentTickerPriceLevel = DefaultPriceLevel.from(TICKER, PRICE, PriceLevelDirection.RISES_ABOVE);
        assertThat(sut.compareTo(differentTickerPriceLevel))
                .isNegative();
    }

    @Test
    void testHashCode() {
        assertThat(sut.hashCode())
                .isEqualTo(Objects.hash(sut.getTicker(), sut.getPrice(), sut.tradeIf()));
    }

    @Test
    void testEquals() {
        assertThat(sut.equals(DefaultPriceLevel.from(TICKER, PRICE, PRICE_LEVEL_DIRECTION)))
                .isTrue();
    }

    @Test
    void testToString() {
        assertThat(sut.toString()).contains("DefaultPriceLevel", TICKER.toString(), String.valueOf(PRICE), PRICE_LEVEL_DIRECTION.name());
    }
}
