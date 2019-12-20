package theta;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

@ExtendWith(MockitoExtension.class)
public class ThetaEngineTest {

  @Mock
  private ConnectionManager mockConnectionManager;
  @Mock
  private PortfolioManager mockPortfolioManager;
  @Mock
  private TickManager mockTickManager;
  @Mock
  private ExecutionManager mockExecutionManager;

  private ThetaEngine sut = null;

  /**
   * Adds responses for several mocks to aid in tests.
   */
  @BeforeEach
  public void setup() {
    when(mockConnectionManager.connect()).thenReturn(Single.just(Instant.now()));
    when(mockPortfolioManager.startPositionProcessing()).thenReturn(Completable.complete());
    when(mockPortfolioManager.getPositionEnd()).thenReturn(Completable.complete());
    when(mockTickManager.startTickProcessing()).thenReturn(Completable.complete());

    sut = new ThetaEngine(mockConnectionManager, mockPortfolioManager, mockTickManager,
        mockExecutionManager);
  }

  @AfterEach
  public void cleanup() {
    sut.shutdown();
  }

  @Test
  public void testRun() throws Exception {

    final String args = "";
    sut.run(args);

    verify(mockConnectionManager).connect();
    verify(mockPortfolioManager).startPositionProcessing();
    verify(mockPortfolioManager).getPositionEnd();
    verify(mockTickManager).startTickProcessing();
  }

  @Test
  public void testShutdown() throws Exception {

    final String args = "";
    sut.run(args);

    // sut.shutdown();

    verify(mockConnectionManager).shutdown();
    verify(mockPortfolioManager).shutdown();
    verify(mockTickManager).shutdown();
    verify(mockExecutionManager).shutdown();
  }

}
