package theta;

import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.reactivex.Completable;
import io.reactivex.Single;
import theta.connection.domain.ConnectionState;
import theta.connection.manager.ConnectionManager;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void setup() {
    sut = new ThetaEngine(mockConnectionManager, mockPortfolioManager, mockTickManager, mockExecutionManager);
  }

  @Ignore
  @Test
  public void testCallReturnSuccessful() {
    Mockito.when(mockConnectionManager.waitUntil(ConnectionState.CONNECTED))
        .thenReturn(Single.just(ZonedDateTime.now()));

    Mockito.when(mockPortfolioManager.getPositionEnd()).thenReturn(Completable.complete());

    sut.run();
    // MatcherAssert.assertThat(returnValue, Matchers.is(Matchers.equalTo("Completed startup")));
  }

  @Ignore
  @Test
  public void testCrossRegistration() {
    Mockito.when(mockConnectionManager.waitUntil(ConnectionState.CONNECTED))
        .thenReturn(Single.just(ZonedDateTime.now()));

    Mockito.when(mockPortfolioManager.getPositionEnd()).thenReturn(Completable.complete());

    sut.run();

    Mockito.verify(mockPortfolioManager).registerTickMonitor(mockTickManager);

    Mockito.verify(mockTickManager).registerPositionProvider(mockPortfolioManager);
    Mockito.verify(mockTickManager).registerExecutor(mockExecutionManager);
  }

}
