package theta.tick.manager;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.util.Precision;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.Theta;
import theta.domain.ThetaTradeTest;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Tick;
import theta.tick.domain.LastTick;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TickManagerTest {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static Double aroundPricePlusMinus = 0.05;

  private static Integer numberOfPriceTicks = 10000;

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

  private Integer calculatePriceTransitions(final Double strikePrice, final List<Double> list) {
    Integer priceTransitions = 0;
    Boolean isPositionLong = Boolean.TRUE;

    for (Integer i = 0; i < list.size(); i++) {
      final Double currentTick = list.get(i);

      if ((currentTick > strikePrice) && !isPositionLong) {
        priceTransitions++;
        isPositionLong = !isPositionLong;
      } else if ((currentTick < strikePrice) && isPositionLong) {
        priceTransitions++;
        isPositionLong = !isPositionLong;
      } else {
        TickManagerTest.logger.error("Possible miscalculated tick");
      }
    }

    return priceTransitions;
  }

  private ArrayList<Tick> generatePriceTicksAround(final Integer numberOfTicks, final Theta theta) {
    final ArrayList<Tick> priceTicks = new ArrayList<Tick>();

    for (Integer i = 0; i < numberOfTicks; i++) {
      final double min = theta.getPrice() - TickManagerTest.aroundPricePlusMinus;
      final double max = theta.getPrice() + TickManagerTest.aroundPricePlusMinus;
      final double randomAroundPrice = ThreadLocalRandom.current().nextDouble(min, max);
      priceTicks.add(new LastTick(theta.getTicker(), Precision.round(randomAroundPrice, 2),
          Precision.round(randomAroundPrice, 2), Precision.round(randomAroundPrice, 2), ZonedDateTime.now()));
    }

    return priceTicks;
  }

  @Ignore
  @Test
  public void test_add_and_delete_monitor() {
    final Theta trade = ThetaTradeTest.buildTestThetaTrade();
    TickManagerTest.logger.debug("Trade to Monitor: {}", trade);

    Mockito.when(handler.getTicker()).thenReturn(trade.getTicker());
    // FIXME
    // Mockito.when(tickSubscriber.addPriceLevelMonitor(trade,
    // ArgumentMatchers.any(TickConsumer.class))).thenReturn(1);

    // sut.addMonitor(trade);

    // final Integer remainingPriceLevels = sut.deleteMonitor(trade);
    TickManagerTest.logger.debug("Monitor removed from Tick Manager for Theta: {}", trade);

    // MatcherAssert.assertThat(remainingPriceLevels, Matchers.is(Matchers.equalTo(0)));
  }

  @Ignore
  @Test
  public void test_ticks_around_strike_price() {
    final Theta trade = ThetaTradeTest.buildTestThetaTrade();
    TickManagerTest.logger.debug("Trade initialized: {}", trade);

    final List<Theta> tradeToReturn = Arrays.asList(trade);
    Mockito.when(positonProvider.providePositions(trade.getTicker())).thenReturn(tradeToReturn);
    // Mockito.when(tickSubscriber.addPriceLevelMonitor(trade,
    // ArgumentMatchers.any(TickConsumer.class))).thenReturn(1);

    // sut.addMonitor(trade);

    final ArrayList<Tick> priceTicks = generatePriceTicksAround(TickManagerTest.numberOfPriceTicks, trade);

    for (final Tick tick : priceTicks) {
      sut.acceptTick(tick.getTicker());
    }

    // Mockito
    // .verify(executor,
    // Mockito.times(calculatePriceTransitions(trade.getStrikePrice(),
    // priceTicks.stream().map(Tick::getLastPrice).collect(Collectors.toList()))))
    // .reverseTrade(ArgumentMatchers.any(Stock.class));
  }
}
