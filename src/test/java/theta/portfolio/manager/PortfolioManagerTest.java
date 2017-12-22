package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Flowable;
import theta.ThetaSchedulersFactory;
import theta.api.PositionHandler;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.execution.api.ExecutionMonitor;
import theta.tick.api.TickMonitor;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PortfolioManagerTest {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Captor
  ArgumentCaptor<ThetaTrade> thetaListCaptor;

  @Mock
  private PositionHandler mockPositionHandler;

  @Mock
  private TickMonitor mockTickManager;

  @Mock
  private ExecutionMonitor mockExecutionMonitor;

  private PortfolioManager sut;

  @Before
  public void initializeManager() {
    sut = new PortfolioManager(mockPositionHandler);
    sut.registerTickMonitor(mockTickManager);
    sut.registerExecutionMonitor(mockExecutionMonitor);

    Flowable.fromCallable(sut).subscribeOn(ThetaSchedulersFactory.getManagerThread()).subscribe();
  }

  @After
  public void shutdownManager() {
    sut.shutdown();
  }

  @Test
  public void ingest_trades_in_order() {
    final int expectedThetas = 6;
    final List<ThetaTrade> thetas = fileIngestHelper("load_trades_in_order.csv", expectedThetas);

    final List<Integer> quantities = thetas.stream().map(ThetaTrade::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(-1, -2, 1, 5, 7, 10).toArray()));
  }

  @Test
  public void ingest_trades_out_of_order() {
    final int expectedThetas = 6;
    final List<ThetaTrade> thetas = fileIngestHelper("load_trades_out_of_order.csv", expectedThetas);

    final List<Integer> quantities = thetas.stream().map(ThetaTrade::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(-7, -1, 1, 2, 5, 10).toArray()));
  }

  @Test
  public void ingest_trades_with_multiple_strike_prices() {
    final int expectedThetas = 4;
    final List<ThetaTrade> thetas = fileIngestHelper("single_ticker_multiple_strike_prices.csv", expectedThetas);

    final List<Integer> quantities = thetas.stream().map(ThetaTrade::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(1, 2, 2, 5).toArray()));
  }

  private List<ThetaTrade> fileIngestHelper(final String filename, int expected) {
    final List<Security> securitiesList = PortfolioTestUtil.readInputFile(filename);

    for (final Security security : securitiesList) {
      PortfolioManagerTest.logger.debug("Trade: {}", security);

      sut.acceptPosition(security);
    }

    Mockito.verify(mockTickManager, Mockito.timeout(5000).times(expected)).addMonitor(thetaListCaptor.capture());

    return thetaListCaptor.getAllValues();
  }
}
