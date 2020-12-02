package theta.domain.util;

import org.junit.jupiter.api.Test;
import theta.domain.SecurityType;
import theta.domain.composed.Theta;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class StockUtilTest {

    private static final Stock STOCK_100 = Stock.of(DefaultTicker.from("ABC"), 100L, 1.12);
    private static final Stock STOCK_200 = Stock.of(DefaultTicker.from("ABC"), 200L, 1.12);

    private static final Option CALL = new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("ABC"), -1L, 1.5, LocalDate.of(2020, 11, 20), 0.21);
    private static final Option PUT = new Option(UUID.randomUUID(), SecurityType.PUT, DefaultTicker.from("ABC"), -1L, 1.5, LocalDate.of(2020, 11, 20), 0.21);
    private static final Theta THETA_1 = Theta.of(STOCK_100, CALL, PUT);
    private static final Theta THETA_2 = Theta.of(STOCK_100, CALL, PUT);

    @Test
    void adjustStockQuantity_exactly100() {
        Optional<Stock> actualOptionalStock = StockUtil.adjustStockQuantity(STOCK_100, 100L);
        assertThat(actualOptionalStock).isEqualTo(Optional.of(STOCK_100));
    }

    @Test
    void adjustStockQuantity_has200expect100() {
        Optional<Stock> actualOptionalStock = StockUtil.adjustStockQuantity(STOCK_200, 100L);
        assertThat(actualOptionalStock).isEqualTo(Optional.of(STOCK_100));
    }

    @Test
    void adjustStockQuantity_has100expect200_getEmpty() {
        Optional<Stock> actualOptionalStock = StockUtil.adjustStockQuantity(STOCK_100, 200L);
        assertThat(actualOptionalStock).isEqualTo(Optional.empty());
    }

    @Test
    void consolidateStock_singleTicker() {
        List<Stock> actualConsolidatedStock = StockUtil.consolidateStock(List.of(THETA_1, THETA_2));
        assertThat(actualConsolidatedStock).containsAll(List.of(STOCK_200));
    }

    @Test
    void consolidateStock_multipleTickers() {
        Stock xyzStock = Stock.of(DefaultTicker.from("XYZ"), 100L, 2.99);
        Option xyzCall = new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("XYZ"), -1L, 33.11, LocalDate.of(2020, 11, 22), 0.43);
        Option xyzPut = new Option(UUID.randomUUID(), SecurityType.PUT, DefaultTicker.from("XYZ"), -1L, 33.11, LocalDate.of(2020, 11, 22), 0.43);
        Theta xyzTheta = Theta.of(xyzStock, xyzCall, xyzPut);

        List<Stock> actualConsolidatedStock = StockUtil.consolidateStock(List.of(THETA_1, THETA_2, xyzTheta));
        assertThat(actualConsolidatedStock).containsAll(List.of(STOCK_200, xyzStock));
    }
}
