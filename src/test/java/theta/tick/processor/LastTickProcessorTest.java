package theta.tick.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.pricelevel.DefaultPriceLevel;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutionType;
import theta.execution.domain.CandidateStockOrder;
import theta.tick.api.Tick;
import theta.tick.domain.DefaultTick;
import theta.tick.domain.TickType;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LastTickProcessorTest {

    private LastTickProcessor sut;

    private static final Tick TICK_ABOVE = new DefaultTick(DefaultTicker.from("ABC"), TickType.LAST, 10.01, 0.0, 0.0, Instant.now());
    private static final Tick TICK_BELOW = new DefaultTick(DefaultTicker.from("ABC"), TickType.LAST, 9.99, 0.0, 0.0, Instant.now());
    private static final PriceLevel PRICE_LEVEL_RISES_ABOVE = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 10.0, PriceLevelDirection.RISES_ABOVE);
    private static final PriceLevel PRICE_LEVEL_FALLS_BELOW = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 10.0, PriceLevelDirection.FALLS_BELOW);

    @BeforeEach
    void setup() {
        sut = new LastTickProcessor();
    }

    @Test
    void isApplicable_last() {
        assertThat(sut.isApplicable(TickType.LAST)).isTrue();
    }

    @Test
    void isApplicable_bid() {
        assertThat(sut.isApplicable(TickType.BID)).isFalse();
    }

    @Test
    void isApplicable_ask() {
        assertThat(sut.isApplicable(TickType.ASK)).isFalse();
    }

    @Test
    void processTick_tickAbovePriceLevelRisesAbove_expectTrue() {
        boolean shouldTrade = sut.processTick(TICK_ABOVE, PRICE_LEVEL_RISES_ABOVE);
        assertThat(shouldTrade).isTrue();
    }

    @Test
    void processTick_tickAbovePriceLevelFallsBelow_expectFalse() {
        boolean shouldTrade = sut.processTick(TICK_ABOVE, PRICE_LEVEL_FALLS_BELOW);
        assertThat(shouldTrade).isFalse();
    }

    @Test
    void processTick_tickBelowPriceLevelRisesAbove_expectFalse() {
        boolean shouldTrade = sut.processTick(TICK_BELOW, PRICE_LEVEL_RISES_ABOVE);
        assertThat(shouldTrade).isFalse();
    }

    @Test
    void processTick_tickBelowPriceLevelFallsBelow_expectTrue() {
        boolean shouldTrade = sut.processTick(TICK_BELOW, PRICE_LEVEL_FALLS_BELOW);
        assertThat(shouldTrade).isTrue();
    }

    @Test
    void getCandidateStockOrder() {
        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 22.34);
        CandidateStockOrder expectedCandidateOrder = new CandidateStockOrder(stock, ExecutionType.MARKET, Optional.empty());

        assertThat(sut.getCandidateStockOrder(stock)).isEqualTo(expectedCandidateOrder);
    }
}
