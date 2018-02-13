package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.Theta;
import theta.domain.ThetaTradeTest;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TickManagerTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Mock
  private Executor executor;
  @Mock
  TickHandler handler;
  @Mock
  PositionProvider positonProvider;
  @InjectMocks
  private TickManager sut;

  @Mock
  private TickSubscriber tickSubscriber;

  @Ignore
  @Test
  public void test_add_and_delete_monitor() {
    final Theta trade = ThetaTradeTest.buildTestThetaTrade();
    TickManagerTest.logger.debug("Trade to Monitor: {}", trade);

    // Mockito.when(handler.getTicker()).thenReturn(trade.getTicker());
    // FIXME
    // Mockito.when(tickSubscriber.addPriceLevelMonitor(trade,
    // ArgumentMatchers.any(TickConsumer.class))).thenReturn(1);

    // sut.addMonitor(trade);

    // final Integer remainingPriceLevels = sut.deleteMonitor(trade);
    TickManagerTest.logger.debug("Monitor removed from Tick Manager for Theta: {}", trade);

    // MatcherAssert.assertThat(remainingPriceLevels, Matchers.is(Matchers.equalTo(0)));
  }

}
