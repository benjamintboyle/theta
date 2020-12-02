package brokers.interactive_brokers.tick.handler;

import com.ib.client.TickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.domain.ticker.DefaultTicker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbTickHandlerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofMillis(1000L);

    @Mock
    private TickProcessor mockTickProcessor;
    @Mock
    private PriceLevel mockPriceLevel;

    private IbTickHandler sut;

    private final Ticker TICKER = DefaultTicker.from("ABC");

    @BeforeEach
    void setup() {
        sut = new IbTickHandler(TICKER, mockTickProcessor);
    }

    @Test
    void getTicks() {
        when(mockTickProcessor.isApplicable(eq(theta.tick.domain.TickType.LAST))).thenReturn(true);
        when(mockTickProcessor.processTick(isA(Tick.class), isA(PriceLevel.class))).thenReturn(true);
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);

        Instant lastTimestamp = Instant.now().with(ChronoField.NANO_OF_SECOND, 0);

        Tick expectTick = new DefaultTick(TICKER, theta.tick.domain.TickType.LAST, 10.99,
                -1.0, -1.0, lastTimestamp);

        Flux<Tick> tickFlux = sut.getTicks();

        sut.addPriceLevelMonitor(mockPriceLevel);
        sut.tickString(TickType.LAST_TIMESTAMP, String.valueOf(lastTimestamp.getEpochSecond()));
        sut.tickPrice(TickType.LAST, 10.99, 0);
        sut.cancel();

        StepVerifier.create(tickFlux)
                .expectNext(expectTick)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void tickPrice() {
        when(mockTickProcessor.isApplicable(isA(theta.tick.domain.TickType.class))).thenReturn(true);
        when(mockTickProcessor.processTick(isA(Tick.class), isA(PriceLevel.class))).thenReturn(true);
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);

        Instant lastTimestamp = Instant.now();
        double price = 10.99;

        Flux<Tick> tickFlux = sut.getTicks();

        sut.addPriceLevelMonitor(mockPriceLevel);
        sut.tickPrice(TickType.BID, price, 0);
        sut.tickPrice(TickType.CLOSE, 11.11, 0);

        Tick expectTickBid = new DefaultTick(TICKER, theta.tick.domain.TickType.BID, -1.0,
                price, -1.0, lastTimestamp);
        Tick expectTickAsk = new DefaultTick(TICKER, theta.tick.domain.TickType.ASK, -1.0,
                price, price, lastTimestamp);

        // Compared without timestamp
        StepVerifier.create(tickFlux)
                .then(() -> sut.tickPrice(TickType.BID, price, 0))
                .expectNextMatches(actual -> actual.getTicker().equals(expectTickBid.getTicker())
                        && actual.getTickType().equals(expectTickBid.getTickType())
                        && Double.compare(actual.getLastPrice(), expectTickBid.getLastPrice()) == 0
                        && Double.compare(actual.getBidPrice(), expectTickBid.getBidPrice()) == 0
                        && Double.compare(actual.getAskPrice(), expectTickBid.getAskPrice()) == 0
                )
                .then(() -> {
                    sut.tickPrice(TickType.ASK_EFP_COMPUTATION, 11.11, 0);
                    sut.tickPrice(TickType.ASK, price, 0);
                    sut.cancel();
                })
                .expectNextMatches(actual -> actual.getTicker().equals(expectTickAsk.getTicker())
                        && actual.getTickType().equals(expectTickAsk.getTickType())
                        && Double.compare(actual.getLastPrice(), expectTickAsk.getLastPrice()) == 0
                        && Double.compare(actual.getBidPrice(), expectTickAsk.getBidPrice()) == 0
                        && Double.compare(actual.getAskPrice(), expectTickAsk.getAskPrice()) == 0
                )
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
    }

    @Test
    void getTicker() {
        assertThat(sut.getTicker()).isEqualTo(TICKER);
    }

    @Test
    void addPriceLevelMonitor_count() {
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);
        sut.addPriceLevelMonitor(mockPriceLevel);
        assertThat(sut.addPriceLevelMonitor(mockPriceLevel)).isEqualTo(1);
    }

    @Test
    void removePriceLevelMonitor_count() {
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);
        sut.addPriceLevelMonitor(mockPriceLevel);
        assertThat(sut.removePriceLevelMonitor(mockPriceLevel)).isEqualTo(0);
    }

    @Test
    void removePriceLevelMonitor_cancelFailure() {
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);
        sut.addPriceLevelMonitor(mockPriceLevel);

        // called twice, so canceled twice. results in exception
        sut.removePriceLevelMonitor(mockPriceLevel);
        assertThatExceptionOfType(Sinks.EmissionException.class)
                .isThrownBy(() -> sut.removePriceLevelMonitor(mockPriceLevel))
                .withMessage("Sink emission failed with FAIL_TERMINATED");
    }

    @Test
    void getPriceLevelsMonitored() {
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);
        sut.addPriceLevelMonitor(mockPriceLevel);
        assertThat(sut.getPriceLevelsMonitored()).isEqualTo(Set.of(mockPriceLevel));
    }
}
