package theta.domain.composed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ShortStraddleTest {

    private ShortStraddle sut;

    @BeforeEach
    void setup() {
        Option call = new Option(UUID.randomUUID(), SecurityType.CALL, DefaultTicker.from("ABC"), -2L, 34.5, LocalDate.of(2020, 11, 25), 1.3);
        Option put = new Option(UUID.randomUUID(), SecurityType.PUT, DefaultTicker.from("ABC"), -2L, 34.5, LocalDate.of(2020, 11, 25), 1.7);
        sut = ShortStraddle.of(call, put);
    }

    @Test
    void getSecurityType() {
        assertThat(sut.getSecurityType()).isEqualTo(SecurityType.SHORT_STRADDLE);
    }

    @Test
    void getTicker() {
        assertThat(sut.getTicker()).isEqualTo(DefaultTicker.from("ABC"));
    }

    @Test
    void getQuantity() {
        assertThat(sut.getQuantity()).isEqualTo(2L);
    }

    @Test
    void getPrice() {
        assertThat(sut.getPrice()).isEqualTo(34.5);
    }

    @Test
    void getStrikePrice() {
        assertThat(sut.getStrikePrice()).isEqualTo(34.5);
    }

    @Test
    void getCall() {
        assertThat(sut.getCall().getSecurityType()).isEqualTo(SecurityType.CALL);
    }

    @Test
    void getPut() {
        assertThat(sut.getPut().getSecurityType()).isEqualTo(SecurityType.PUT);
    }

    @Test
    void checkValid_tickersNotMatching() {
        Option callNotMatchingTicker = new Option(sut.getCall().getId(), sut.getCall().getSecurityType(), DefaultTicker.from("XYZ"), sut.getCall().getQuantity(), sut.getCall().getStrikePrice(), sut.getCall().getExpiration(), sut.getCall().getAverageTradePrice());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(callNotMatchingTicker, sut.getPut()))
                .withMessageContaining("Short Straddle - Tickers don't match.");
    }

    @Test
    void checkValid_invalidTwoPuts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getPut(), sut.getPut()))
                .withMessage("Short Straddle - Call assigned is actually PUT");
    }

    @Test
    void checkValid_invalidTwoCalls() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getCall(), sut.getCall()))
                .withMessage("Short Straddle - Put assigned is actually CALL");
    }

    @Test
    void checkValid_invalidDifferentQuantities() {
        Option putWithDifferentQuantity = new Option(sut.getPut().getId(), sut.getPut().getSecurityType(), sut.getPut().getTicker(), -1L, sut.getPut().getStrikePrice(), sut.getPut().getExpiration(), sut.getPut().getAverageTradePrice());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getCall(), putWithDifferentQuantity))
                .withMessageContaining("Short Straddle - Call and Put quantities do not match.");
    }

    @Test
    void checkValid_invalidQuantitiesAreNotShort() {
        Option putWithPositiveQuantity = new Option(sut.getPut().getId(), sut.getPut().getSecurityType(), sut.getPut().getTicker(), 2L, sut.getPut().getStrikePrice(), sut.getPut().getExpiration(), sut.getPut().getAverageTradePrice());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getCall(), putWithPositiveQuantity))
                .withMessageContaining("Short Straddle - Call and/or Put quantities are not less than zero.");
    }

    @Test
    void checkValid_invalidStrikePrice() {
        Option putWithZeroStrikePrice = new Option(sut.getPut().getId(), sut.getPut().getSecurityType(), sut.getPut().getTicker(), sut.getPut().getQuantity(), 0.0, sut.getPut().getExpiration(), sut.getPut().getAverageTradePrice());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getCall(), putWithZeroStrikePrice))
                .withMessageContaining("Short Straddle - Call and Put strike prices do not match.");
    }

    @Test
    void checkValid_invalidExpiration() {
        Option putWithZeroStrikePrice = new Option(sut.getPut().getId(), sut.getPut().getSecurityType(), sut.getPut().getTicker(), sut.getPut().getQuantity(), sut.getPut().getStrikePrice(), LocalDate.EPOCH, sut.getPut().getAverageTradePrice());
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ShortStraddle.of(sut.getCall(), putWithZeroStrikePrice))
                .withMessageContaining("Short Straddle - Call and Put expirations do not match.");
    }
}
