package theta.tick.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import theta.api.TickSubscriber;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.composed.Theta;
import theta.domain.manager.ManagerState;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.Executor;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;
import theta.tick.domain.TickType;
import theta.util.MarketUtility;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TickManagerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private TickSubscriber mockTickSubscriber;
    @Mock
    private TickProcessor mockTickProcessor;
    @Mock
    private Executor mockExecutor;
    @Mock
    private MarketUtility mockMarketUtility;

    private TickManager sut;

    @BeforeEach
    void setup() {
        sut = new TickManager(mockTickSubscriber, mockTickProcessor, mockExecutor, mockMarketUtility);
    }

    @Test
    void startTickProcessing_noTick() {
        TestPublisher<Tick> ticksAcross = TestPublisher.create();
        when(mockTickSubscriber.getTicksAcrossStrikePrices()).thenReturn(ticksAcross.flux());

        StepVerifier.create(sut.startTickProcessing())
                .then(ticksAcross::complete)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void startTickProcessing_exception() {
        TestPublisher<Tick> ticksAcross = TestPublisher.create();
        when(mockTickSubscriber.getTicksAcrossStrikePrices()).thenReturn(ticksAcross.flux());

        StepVerifier.create(sut.startTickProcessing())
                .then(() -> ticksAcross.error(new NullPointerException("Test Exception")))
                .expectError(NullPointerException.class)
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void startTickProcessing_singleTick() {
        final Ticker ticker = DefaultTicker.from("ABC");
        final Stock stock = Stock.of(ticker, 100L, 123.45);
        final Option call = new Option(UUID.randomUUID(), SecurityType.CALL, ticker, -1L, 50.0,
                LocalDate.of(2020, 10, 30), 1.1);
        final Option put = new Option(UUID.randomUUID(), SecurityType.PUT, ticker, -1L, 50.0,
                LocalDate.of(2020, 10, 30), 1.1);
        Theta theta = Theta.of(stock, call, put);

        DefaultTick tick = new DefaultTick(
                ticker,
                TickType.ASK,
                1.0,
                1.0,
                1.0,
                Instant.now().minusSeconds(1L));

        TestPublisher<Tick> ticksAcross = TestPublisher.create();
        when(mockTickSubscriber.getTicksAcrossStrikePrices()).thenReturn(ticksAcross.flux());
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);
        when(mockTickProcessor.processTick(any(), any())).thenReturn(true);
        when(mockExecutor.reverseTrade(any())).thenReturn(Mono.empty());

        sut.addMonitor(theta);

        StepVerifier.create(sut.startTickProcessing())
                .then(() -> ticksAcross.next(tick))
                .then(ticksAcross::complete)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
        verify(mockExecutor).reverseTrade(any());
    }

    @Test
    void startTickProcessing_singleTick_noPositions() {
        DefaultTick tick = new DefaultTick(
                DefaultTicker.from("ABC"),
                TickType.ASK,
                1.0,
                1.0,
                1.0,
                Instant.now().minusSeconds(1L));

        TestPublisher<Tick> ticksAcross = TestPublisher.create();
        when(mockTickSubscriber.getTicksAcrossStrikePrices()).thenReturn(ticksAcross.flux());
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);

        StepVerifier.create(sut.startTickProcessing())
                .then(() -> ticksAcross.next(tick))
                .then(ticksAcross::complete)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
        verify(mockExecutor).convertToMarketOrderIfExists(any());
    }

    @Test
    void startTickProcessing_singleTick_deletedPositions() {
        final Ticker ticker = DefaultTicker.from("ABC");
        final Stock stock = Stock.of(ticker, 100L, 123.45);
        final Option call = new Option(UUID.randomUUID(), SecurityType.CALL, ticker, -1L, 50.0,
                LocalDate.of(2020, 10, 30), 1.1);
        final Option put = new Option(UUID.randomUUID(), SecurityType.PUT, ticker, -1L, 50.0,
                LocalDate.of(2020, 10, 30), 1.1);
        Theta theta = Theta.of(stock, call, put);

        DefaultTick tick = new DefaultTick(
                ticker,
                TickType.ASK,
                1.0,
                1.0,
                1.0,
                Instant.now().minusSeconds(1L));

        TestPublisher<Tick> ticksAcross = TestPublisher.create();
        when(mockTickSubscriber.getTicksAcrossStrikePrices()).thenReturn(ticksAcross.flux());
        when(mockMarketUtility.isDuringMarketHours(any())).thenReturn(true);

        sut.addMonitor(theta);
        sut.deleteMonitor(theta);

        StepVerifier.create(sut.startTickProcessing())
                .then(() -> ticksAcross.next(tick))
                .then(ticksAcross::complete)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
        verify(mockExecutor, never()).reverseTrade(any());
        verify(mockExecutor).convertToMarketOrderIfExists(any());
    }

    @Test
    void shutdown() {
        sut.shutdown();
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.STOPPING);
    }
}
