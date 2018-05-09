package theta.tick.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import io.reactivex.observers.TestObserver;
import theta.api.TickSubscriber;
import theta.domain.DefaultPriceLevel;
import theta.domain.ManagerState;
import theta.domain.ManagerStatus;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.TickProcessor;

public class TickManagerTest2 {

  @Mock
  private TickSubscriber mockTickSubscriber;
  @Mock
  private TickProcessor mockTickProcessor;

  @Mock
  private PositionProvider mockPositionProvider;
  @Mock
  private Executor mockExecutor;

  private TickManager sut = null;

  @BeforeEach
  public void setup() {

    sut = new TickManager(mockTickSubscriber, mockTickProcessor);

    sut.registerPositionProvider(mockPositionProvider);
    sut.registerExecutor(mockExecutor);
  }

  @Disabled
  @Test
  public void testTickManagerStateStarting() {
    assertThat("After construction, Tick Manager should be in STARTING state.", sut.getStatus().getState(),
        is(equalTo(ManagerState.STARTING)));
  }

  @Disabled
  @Test
  public void testTickManagerStateRunning() {

    TestObserver<Void> testCompletableObserver = sut.startTickProcessing().test();

    testCompletableObserver.assertNotComplete();
    assertThat("After started, Tick Manager should be in RUNNING state.", sut.getStatus().getState(),
        is(equalTo(ManagerState.RUNNING)));
  }

  @Disabled
  @Test
  public void testStartTickProcessing() {

    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testAddMonitor() {
    PriceLevel priceLevel = DefaultPriceLevel.from(Ticker.from("ABC"), 100.0, PriceLevelDirection.FALLS_BELOW);

    sut.addMonitor(priceLevel);

    verify(mockTickSubscriber).addPriceLevelMonitor(priceLevel, mockTickProcessor);
  }

  @Disabled
  @Test
  public void testDeleteMonitor() {
    PriceLevel priceLevel = DefaultPriceLevel.from(Ticker.from("ABC"), 100.0, PriceLevelDirection.FALLS_BELOW);

    sut.deleteMonitor(priceLevel);

    verify(mockTickSubscriber).removePriceLevelMonitor(priceLevel);
  }

  @Disabled
  @Test
  public void testGetStatus() {
    ManagerStatus tickManagerStatus = sut.getStatus();

    assertThat("Call to initialized Tick Manager getStatus should result in STARTING state.",
        tickManagerStatus.getState(), is(equalTo(ManagerState.STARTING)));
    assertThat("Call to initialized Tick Manager getStatus should result in timestamp within last second.",
        tickManagerStatus.getTime(), is(greaterThan(ZonedDateTime.now().minusSeconds(1))));
    assertThat("Call to initialized Tick Manager getStatus should be before time now.", tickManagerStatus.getTime(),
        is(lessThan(ZonedDateTime.now())));
  }

  @Disabled
  @Test
  public void testShutdownStateStopping() {
    sut.shutdown();

    assertThat("After shutdown, Tick Manager should be in STOPPING state.", sut.getStatus().getState(),
        is(equalTo(ManagerState.STOPPING)));
  }

  @Disabled
  @Test
  public void testShutdownStateShutdown() {
    sut.startTickProcessing();
    sut.shutdown();

    assertThat("After shutdown, Tick Manager should be in STOPPING state.", sut.getStatus().getState(),
        is(equalTo(ManagerState.SHUTDOWN)));
  }

}
