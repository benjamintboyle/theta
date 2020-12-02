package theta.tick.processor;

import org.assertj.core.api.SoftAssertions;
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

import static org.assertj.core.api.Assertions.assertThat;

class BidAskSpreadTickProcessorTest {

    private BidAskSpreadTickProcessor sut;

    @BeforeEach
    void setup() {
        sut = new BidAskSpreadTickProcessor();
    }

    @Test
    void isApplicable_last() {
        assertThat(sut.isApplicable(TickType.LAST)).isFalse();
    }

    @Test
    void isApplicable_ask() {
        assertThat(sut.isApplicable(TickType.ASK)).isTrue();
    }

    @Test
    void isApplicable_bid() {
        assertThat(sut.isApplicable(TickType.BID)).isTrue();
    }

    @Test
    void processTick_fallsBelow_shouldTrade() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.ASK, 0.9, 0.3, 1.3, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.FALLS_BELOW);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        assertThat(shouldOrder).isTrue();
    }

    @Test
    void processTick_fallsBelow_belowAskPrice() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.ASK, 0.9, 0.3, 0.9, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.FALLS_BELOW);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 22.4);
        CandidateStockOrder candidateOrder = sut.getCandidateStockOrder(stock);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(shouldOrder).isTrue();
            softly.assertThat(candidateOrder.executionType()).isEqualTo(ExecutionType.LIMIT);
            softly.assertThat(candidateOrder.limitPrice().isPresent()).isTrue();
            softly.assertThat(candidateOrder.limitPrice().get()).isLessThan(priceLevel.getPrice());
        });
    }

    @Test
    void processTick_fallsBelow_shouldNotTrade() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.ASK, 0.9, 0.3, 1.4, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.FALLS_BELOW);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        assertThat(shouldOrder).isFalse();
    }

    @Test
    void processTick_risesAbove_shouldTrade() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.BID, 0.9, 0.7, 1.7, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.RISES_ABOVE);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        assertThat(shouldOrder).isTrue();
    }

    @Test
    void processTick_risesAbove_aboveBidPrice() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.BID, 0.9, 1.1, 1.7, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.RISES_ABOVE);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 22.4);
        CandidateStockOrder candidateOrder = sut.getCandidateStockOrder(stock);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(shouldOrder).isTrue();
            softly.assertThat(candidateOrder.executionType()).isEqualTo(ExecutionType.LIMIT);
            softly.assertThat(candidateOrder.limitPrice().isPresent()).isTrue();
            softly.assertThat(candidateOrder.limitPrice().get()).isGreaterThan(priceLevel.getPrice());
        });
    }

    @Test
    void processTick_risesAbove_shouldNotTrade() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.BID, 0.9, 0.6, 1.6, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.RISES_ABOVE);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        assertThat(shouldOrder).isFalse();
    }

    @Test
    void getCandidateStockOrder() {
        Tick tick = new DefaultTick(DefaultTicker.from("ABC"), TickType.ASK, 0.9, 0.3, 1.3, Instant.now());
        PriceLevel priceLevel = DefaultPriceLevel.from(DefaultTicker.from("ABC"), 1.0, PriceLevelDirection.FALLS_BELOW);

        boolean shouldOrder = sut.processTick(tick, priceLevel);

        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 22.4);
        CandidateStockOrder candidateOrder = sut.getCandidateStockOrder(stock);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(shouldOrder).isTrue();
            softly.assertThat(candidateOrder.executionType()).isEqualTo(ExecutionType.LIMIT);
            softly.assertThat(candidateOrder.limitPrice().isPresent()).isTrue();
            softly.assertThat(candidateOrder.limitPrice().get()).isEqualTo(priceLevel.getPrice());
        });
    }

    @Test
    void getCandidateStockOrder_noLimitPrice() {
        Stock stock = Stock.of(DefaultTicker.from("ABC"), 100L, 22.4);
        CandidateStockOrder candidateOrder = sut.getCandidateStockOrder(stock);

        assertThat(candidateOrder.executionType()).isEqualTo(ExecutionType.MARKET);
    }
}
