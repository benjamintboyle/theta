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
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.PositionHandler;
import theta.domain.Theta;
import theta.domain.api.Security;
import theta.tick.api.TickMonitor;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PortfolioManagerTest {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Captor
  ArgumentCaptor<Theta> thetaListCaptor;

  @Mock
  private PositionHandler mockPositionHandler;

  @Mock
  private TickMonitor mockTickManager;

  private PortfolioManager sut;

  @Before
  public void initializeManager() {
    sut = new PortfolioManager(mockPositionHandler);
    sut.registerTickMonitor(mockTickManager);

    sut.startPositionProcessing().subscribe();
  }

  @After
  public void shutdownManager() {
    sut.shutdown();
  }

  @Test
  public void ingest_trades_in_order() {
    final int expectedThetas = 6;
    final List<Theta> thetas = fileIngestHelper("load_trades_in_order.csv", expectedThetas);

    final List<Long> quantities = thetas.stream().map(Theta::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(-1L, -2L, 1L, 5L, 7L, 10L).toArray()));
  }

  @Test
  public void ingest_trades_out_of_order() {
    final int expectedThetas = 6;
    final List<Theta> thetas = fileIngestHelper("load_trades_out_of_order.csv", expectedThetas);

    final List<Long> quantities = thetas.stream().map(Theta::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(-7L, -1L, 1L, 2L, 5L, 10L).toArray()));
  }

  @Test
  public void ingest_trades_with_multiple_strike_prices() {
    final int expectedThetas = 4;
    final List<Theta> thetas = fileIngestHelper("single_ticker_multiple_strike_prices.csv", expectedThetas);

    final List<Long> quantities = thetas.stream().map(Theta::getQuantity).collect(Collectors.toList());

    MatcherAssert.assertThat(thetas, Matchers.hasSize(expectedThetas));
    MatcherAssert.assertThat(quantities,
        IsIterableContainingInAnyOrder.containsInAnyOrder(List.of(1L, 2L, 2L, 5L).toArray()));
  }

  private List<Theta> fileIngestHelper(final String filename, int expected) {
    final List<Security> securitiesList = PortfolioTestUtil.readInputFile(filename);

    for (final Security security : securitiesList) {
      PortfolioManagerTest.logger.debug("Trade: {}", security);

      // TODO: NEEDS FIXING as acceptPosition() no longer exists
      // sut.acceptPosition(security);
    }

    // Mockito.verify(mockTickManager,
    // Mockito.timeout(5000).times(expected)).addMonitor(thetaListCaptor.capture());

    return thetaListCaptor.getAllValues();
  }
}
