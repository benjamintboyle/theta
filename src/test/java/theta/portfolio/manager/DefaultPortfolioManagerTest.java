package theta.portfolio.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;
import theta.api.PositionHandler;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.composed.Theta;
import theta.domain.manager.ManagerState;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.tick.api.TickMonitor;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPortfolioManagerTest {

    @Mock
    private PositionHandler mockPositionHandler;
    @Mock
    private TickMonitor mockTickMonitor;

    private DefaultPortfolioManager sut;

    @BeforeEach
    void setup() {
        sut = new DefaultPortfolioManager(mockPositionHandler, mockTickMonitor);
    }

    @AfterEach
    void teardown() {
        sut.shutdown();
    }

    @Test
    void startPositionProcessing_successfulTheta() {
        TestPublisher<Security> positionsFromBrokerage = TestPublisher.create();
        when(mockPositionHandler.requestPositionsFromBrokerage())
                .thenReturn(positionsFromBrokerage.flux());

        sut.startPositionProcessing().subscribe();

        positionsFromBrokerage.emit(buildStock(DefaultTicker.from("ABC")), buildCall(), buildPut());
        verify(mockTickMonitor).addMonitor(eq(buildTheta()));
    }

    @Test
    void startPositionProcessing_mismatchedTheta_thenSuccess() {
        TestPublisher<Security> positionsFromBrokerage = TestPublisher.create();
        when(mockPositionHandler.requestPositionsFromBrokerage())
                .thenReturn(positionsFromBrokerage.flux());

        sut.startPositionProcessing().subscribe();

        positionsFromBrokerage.next(buildStock(DefaultTicker.from("XYZ")), buildCall(), buildPut());
        verify(mockTickMonitor, never()).addMonitor(any());
        positionsFromBrokerage.emit(buildStock(DefaultTicker.from("ABC")));
        verify(mockTickMonitor).addMonitor(eq(buildTheta()));
    }

    @Test
    void getStatus_starting() {
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.STARTING);
    }

    @Test
    void getStatus_running() {
        when(mockPositionHandler.requestPositionsFromBrokerage()).thenReturn(Flux.never());
        sut.startPositionProcessing().subscribe();
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.RUNNING);
    }

    @Test
    void getStatus_stopping() {
        when(mockPositionHandler.requestPositionsFromBrokerage()).thenReturn(Flux.never());
        sut.startPositionProcessing().subscribe();
        sut.shutdown();
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.STOPPING);
    }

    @Test
    void shutdown() {
        when(mockPositionHandler.requestPositionsFromBrokerage()).thenReturn(Flux.just());
        sut.startPositionProcessing().subscribe();
        sut.shutdown();
        assertThat(sut.getStatus().getState()).isEqualTo(ManagerState.SHUTDOWN);
    }

    private static Theta buildTheta() {
        return Theta.of(buildStock(DefaultTicker.from("ABC")), buildCall(), buildPut());
    }

    private static Stock buildStock(Ticker ticker) {
        return Stock.of(ticker, 100L, 123.45);
    }

    private static Option buildCall() {
        return buildOption(DefaultTicker.from("ABC"), SecurityType.CALL);
    }

    private static Option buildPut() {
        return buildOption(DefaultTicker.from("ABC"), SecurityType.PUT);
    }

    private static Option buildOption(Ticker ticker, SecurityType securityType) {
        return new Option(UUID.randomUUID(), securityType, ticker, -1L, 123.5, LocalDate.of(1999, 2, 5), 6.78);
    }
}
