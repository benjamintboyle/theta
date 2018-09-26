package theta;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.reactivex.Completable;
import io.reactivex.Single;
import theta.connection.manager.DefaultConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

@ExtendWith(MockitoExtension.class)
public class ThetaEngineTest {

  @Mock
  private DefaultConnectionManager mockConnectionManager;
  @Mock
  private PortfolioManager mockPortfolioManager;
  @Mock
  private TickManager mockTickManager;
  @Mock
  private ExecutionManager mockExecutionManager;

  private ThetaEngine sut = null;

  @BeforeEach
  public void setup() {
    when(mockConnectionManager.connect()).thenReturn(Single.just(Instant.now()));
    when(mockPortfolioManager.startPositionProcessing()).thenReturn(Completable.complete());
    when(mockPortfolioManager.getPositionEnd()).thenReturn(Completable.complete());
    when(mockTickManager.startTickProcessing()).thenReturn(Completable.complete());

    sut = new ThetaEngine(mockConnectionManager, mockPortfolioManager, mockTickManager, mockExecutionManager);
  }

  @AfterEach
  public void cleanup() {
    sut.shutdown();
  }

  @Test
  public void testRun() {

    sut.run();

    verify(mockPortfolioManager).registerTickMonitor(mockTickManager);
    verify(mockTickManager).registerPositionProvider(mockPortfolioManager);
    verify(mockTickManager).registerExecutor(mockExecutionManager);

    verify(mockConnectionManager).connect();
    verify(mockPortfolioManager).startPositionProcessing();
    verify(mockPortfolioManager).getPositionEnd();
    verify(mockTickManager).startTickProcessing();
  }

  @Test
  public void testShutdown() {

    sut.run();
    sut.shutdown();

    verify(mockConnectionManager).shutdown();
    verify(mockPortfolioManager).shutdown();
    verify(mockTickManager).shutdown();
    verify(mockExecutionManager).shutdown();
  }

}
