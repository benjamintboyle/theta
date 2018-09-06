package theta.portfolio.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import theta.api.PositionHandler;
import theta.domain.composed.Theta;
import theta.domain.manager.ManagerState;
import theta.domain.pricelevel.DefaultPriceLevel;
import theta.domain.stock.Stock;
import theta.domain.testutil.ThetaDomainFactory;
import theta.tick.api.TickMonitor;

@ExtendWith(MockitoExtension.class)
class PortfolioManagerTest {

  @Mock
  private PositionHandler positionHandler;

  @Mock
  private TickMonitor monitor;

  private PortfolioManager sut = null;

  @BeforeEach
  void setup() {
    sut = new PortfolioManager(positionHandler);
  }

  @Test
  void testStartPositionProcessing() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();
    when(positionHandler.requestPositionsFromBrokerage())
        .thenReturn(Flowable.just(theta.getStock(), theta.getCall(), theta.getPut()));
    sut.registerTickMonitor(monitor);

    TestObserver<Void> testObserver = sut.startPositionProcessing().test();

    verify(monitor).addMonitor(DefaultPriceLevel.of(theta));
    assertThat(sut.providePositions(theta.getTicker()), is(equalTo(List.of(theta))));
    testObserver.onComplete();
  }

  @Test
  void testZeroQuantityPosition() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();
    when(positionHandler.requestPositionsFromBrokerage())
        .thenReturn(Flowable.just(theta.getStock(), theta.getCall(), theta.getPut(),
            Stock.of(theta.getStock().getId(), theta.getStock().getTicker(), 0L, theta.getStock().getPrice())));
    sut.registerTickMonitor(monitor);

    TestObserver<Void> testObserver = sut.startPositionProcessing().test();

    verify(monitor).addMonitor(DefaultPriceLevel.of(theta));
    assertThat(sut.providePositions(theta.getTicker()), is(emptyCollectionOf(Theta.class)));
    testObserver.onComplete();
  }

  @Test
  void testGetPositionEnd() {

    when(positionHandler.getPositionEnd()).thenReturn(Completable.complete());

    assertThat(sut.getPositionEnd(), is(Completable.complete()));
  }

  @Test
  void testProvidePositions() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();
    when(positionHandler.requestPositionsFromBrokerage())
        .thenReturn(Flowable.just(theta.getStock(), theta.getCall(), theta.getPut()));
    sut.registerTickMonitor(monitor);

    TestObserver<Void> testObserver = sut.startPositionProcessing().test();

    assertThat(sut.providePositions(theta.getTicker()), is(equalTo(List.of(theta))));
    testObserver.onComplete();
  }

  @Test
  void testGetStatus() {

    sut.getStatus().changeState(ManagerState.RUNNING);
    assertThat(sut.getStatus(), is(notNullValue()));
    assertThat(sut.getStatus().getState(), is(equalTo(ManagerState.RUNNING)));
    assertThat(sut.getStatus().getTime(), is(greaterThan(ZonedDateTime.now().minusSeconds(1))));
    assertThat(sut.getStatus().getTime(), is(lessThanOrEqualTo(ZonedDateTime.now())));

    sut.getStatus().changeState(ManagerState.SHUTDOWN);
    assertThat(sut.getStatus(), is(notNullValue()));
    assertThat(sut.getStatus().getState(), is(equalTo(ManagerState.SHUTDOWN)));
    assertThat(sut.getStatus().getTime(), is(greaterThan(ZonedDateTime.now().minusSeconds(1))));
    assertThat(sut.getStatus().getTime(), is(lessThanOrEqualTo(ZonedDateTime.now())));
  }

  @Test
  void testRegisterTickMonitor() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();
    when(positionHandler.requestPositionsFromBrokerage())
        .thenReturn(Flowable.just(theta.getStock(), theta.getCall(), theta.getPut()));
    sut.registerTickMonitor(monitor);

    TestObserver<Void> testObserver = sut.startPositionProcessing().test();

    verify(monitor).addMonitor(DefaultPriceLevel.of(theta));
    testObserver.onComplete();
  }

  @Test
  void testShutdown() {

    assertThat(sut.getStatus().getState(), is(not(ManagerState.STOPPING)));

    sut.shutdown();

    assertThat(sut.getStatus().getState(), is(ManagerState.STOPPING));
  }

}
