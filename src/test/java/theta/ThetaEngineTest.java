package theta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import theta.connection.domain.ConnectionState;
import theta.connection.domain.ConnectionStatus;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManger;
import theta.tick.manager.TickManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThetaEngineTest {

    @Mock
    private ConnectionManager mockConnectionManager;
    @Mock
    private PortfolioManger mockPortfolioManager;
    @Mock
    private TickManager mockTickManager;
    @Mock
    private ExecutionManager mockExecutionManager;

    private ThetaEngine sut;

    @BeforeEach
    void setup() {
        sut = new ThetaEngine(mockConnectionManager, mockPortfolioManager, mockTickManager, mockExecutionManager);
    }

    @Test
    void run_allSuccessfulResponses() {
        when(mockConnectionManager.connect()).thenReturn(Mono.just(ConnectionStatus.of(ConnectionState.CONNECTED)));
        when(mockPortfolioManager.startPositionProcessing()).thenReturn(Mono.empty());
        when(mockTickManager.startTickProcessing()).thenReturn(Mono.empty());

        sut.run();

        verify(mockConnectionManager).connect();
        verify(mockPortfolioManager).startPositionProcessing();
        verify(mockTickManager).startTickProcessing();
    }

    @Test
    void run_connectionError() {
        TestPublisher<ConnectionStatus> connectionPublisher = TestPublisher.create();
        when(mockConnectionManager.connect()).thenReturn(connectionPublisher.mono());
        when(mockPortfolioManager.startPositionProcessing()).thenReturn(Mono.empty());
        when(mockTickManager.startTickProcessing()).thenReturn(Mono.empty());

        sut.run();
        connectionPublisher.error(new Throwable("Manually thrown error for testing purposes"));

        verify(mockConnectionManager).connect();
        verify(mockPortfolioManager).startPositionProcessing();
        verify(mockTickManager).startTickProcessing();

        verify(mockConnectionManager).shutdown();
        verify(mockPortfolioManager).shutdown();
        verify(mockTickManager).shutdown();
        verify(mockExecutionManager).shutdown();
    }

    @Test
    void run_positionError() {
        when(mockConnectionManager.connect()).thenReturn(Mono.just(ConnectionStatus.of(ConnectionState.CONNECTED)));
        TestPublisher<Void> positionPublisher = TestPublisher.create();
        when(mockPortfolioManager.startPositionProcessing()).thenReturn(positionPublisher.mono());
        when(mockTickManager.startTickProcessing()).thenReturn(Mono.empty());

        sut.run();
        positionPublisher.error(new Throwable("Manually thrown error for testing purposes"));

        verify(mockConnectionManager).connect();
        verify(mockPortfolioManager).startPositionProcessing();
        verify(mockTickManager).startTickProcessing();

        verify(mockConnectionManager).shutdown();
        verify(mockPortfolioManager).shutdown();
        verify(mockTickManager).shutdown();
        verify(mockExecutionManager).shutdown();
    }

    @Test
    void run_tickError() {
        when(mockConnectionManager.connect()).thenReturn(Mono.just(ConnectionStatus.of(ConnectionState.CONNECTED)));
        when(mockPortfolioManager.startPositionProcessing()).thenReturn(Mono.empty());
        TestPublisher<Void> tickPublisher = TestPublisher.create();
        when(mockTickManager.startTickProcessing()).thenReturn(tickPublisher.mono());

        sut.run();
        tickPublisher.error(new Throwable("Manually thrown error for testing purposes"));

        verify(mockConnectionManager).connect();
        verify(mockPortfolioManager).startPositionProcessing();
        verify(mockTickManager).startTickProcessing();

        verify(mockConnectionManager).shutdown();
        verify(mockPortfolioManager).shutdown();
        verify(mockTickManager).shutdown();
        verify(mockExecutionManager).shutdown();
    }
}
