package brokers.interactive_brokers.tick;

import com.ib.client.TickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.domain.ticker.DefaultTicker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbTickHandlerTest {

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
                .verifyComplete();
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

        StepVerifier.create(tickFlux)
                .expectNextMatches(actual -> actual.getTicker().equals(expectTickBid.getTicker())
                        && actual.getTickType().equals(expectTickBid.getTickType())
                        && actual.getLastPrice().equals(expectTickBid.getLastPrice())
                        && actual.getBidPrice().equals(expectTickBid.getBidPrice())
                        && actual.getAskPrice().equals(expectTickBid.getAskPrice())
                )
                .then(() -> {
                    sut.tickPrice(TickType.ASK_EFP_COMPUTATION, 11.11, 0);
                    sut.tickPrice(TickType.ASK, price, 0);
                    sut.cancel();
                })
                .expectNextMatches(actual -> actual.getTicker().equals(expectTickAsk.getTicker())
                        && actual.getTickType().equals(expectTickAsk.getTickType())
                        && actual.getLastPrice().equals(expectTickAsk.getLastPrice())
                        && actual.getBidPrice().equals(expectTickAsk.getBidPrice())
                        && actual.getAskPrice().equals(expectTickAsk.getAskPrice())
                )
                .verifyComplete();
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
    void getPriceLevelsMonitored() {
        when(mockPriceLevel.getTicker()).thenReturn(TICKER);
        sut.addPriceLevelMonitor(mockPriceLevel);
        assertThat(sut.getPriceLevelsMonitored()).isEqualTo(Set.of(mockPriceLevel));
    }
}
