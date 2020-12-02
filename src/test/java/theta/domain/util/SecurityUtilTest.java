package theta.domain.util;

import org.junit.jupiter.api.Test;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.composed.Theta;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilTest {

    private static final Stock stock = Stock.of(DefaultTicker.from("ABC"), 200L, 123.45);
    private static final Option option = new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("ABC"), 3L, 7.77,
            LocalDate.of(2020, 10, 25), 0.43);

    @Test
    void getStockWithQuantity_negativeAdjustment() {
        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(stock, -100L);
        assertThat(adjustedSecurity).isEqualTo(Optional.empty());
    }

    @Test
    void getStockWithQuantity_subsetQuantity() {
        final long adjustment = 100L;
        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(stock, adjustment);
        assertThat(adjustedSecurity.orElseThrow()).isEqualTo(Stock.of(stock.getTicker(), adjustment, stock.getPrice()));
    }

    @Test
    void getStockWithQuantity_equalQuantity() {
        final long adjustment = 200L;
        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(stock, adjustment);
        assertThat(adjustedSecurity.orElseThrow()).isEqualTo(Stock.of(stock.getTicker(), adjustment, stock.getPrice()));
    }

    @Test
    void getOptionWithQuantity_subsetQuantity() {
        final long adjustment = 2L;
        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(option, adjustment);
        assertThat(adjustedSecurity.orElseThrow()).isEqualTo(new Option(option.getId(), option.getSecurityType(), option.getTicker(), adjustment, option.getStrikePrice(),
                option.getExpiration(), option.getAverageTradePrice()));
    }

    @Test
    void getOptionWithQuantity_equalQuantity() {
        final long adjustment = 3L;
        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(option, adjustment);
        assertThat(adjustedSecurity.orElseThrow()).isEqualTo(new Option(option.getId(), option.getSecurityType(), option.getTicker(), adjustment, option.getStrikePrice(),
                option.getExpiration(), option.getAverageTradePrice()));
    }

    @Test
    void getSecurityWithQuantity_nonSupportedType() {
        Theta theta = Theta.of(
                Stock.of(stock.getTicker(), 100L, stock.getPrice()),
                new Option(UUID.randomUUID(), SecurityType.CALL, stock.getTicker(), -1L, option.getStrikePrice(),
                        option.getExpiration(), option.getAverageTradePrice()),
                new Option(UUID.randomUUID(), SecurityType.PUT, stock.getTicker(), -1L, option.getStrikePrice(),
                        option.getExpiration(), option.getAverageTradePrice()));

        Optional<Security> adjustedSecurity = SecurityUtil.getSecurityWithQuantity(theta, 1L);
        assertThat(adjustedSecurity).isEqualTo(Optional.empty());
    }
}
