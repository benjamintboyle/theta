package theta.tick.manager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.TickHandler;
import theta.api.TickSubscriber;
import theta.domain.ThetaTrade;
import theta.domain.ThetaTradeTest;
import theta.execution.api.Executor;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.TickObserver;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;

@RunWith(MockitoJUnitRunner.class)
public class TickManagerTest {
	private static Double aroundPricePlusMinus = 0.05;

	private static final Logger logger = LoggerFactory.getLogger(TickManagerTest.class);
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

	private ArrayList<Tick> generatePriceTicksAround(final Integer numberOfTicks, final ThetaTrade theta) {
		final ArrayList<Tick> priceTicks = new ArrayList<Tick>();

		for (Integer i = 0; i < numberOfTicks; i++) {
			final double min = theta.getStrikePrice() - TickManagerTest.aroundPricePlusMinus;
			final double max = theta.getStrikePrice() + TickManagerTest.aroundPricePlusMinus;
			final double randomAroundPrice = ThreadLocalRandom.current().nextDouble(min, max);
			priceTicks.add(new Tick(theta.getTicker(), Precision.round(randomAroundPrice, 2), TickType.LAST,
					ZonedDateTime.now()));
		}

		return priceTicks;
	}

	@Ignore
	@Test
	public void test_add_and_delete_monitor() {
		final ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		TickManagerTest.logger.debug("Trade to Monitor: {}", trade);

		Mockito.when(this.handler.getTicker()).thenReturn(trade.getTicker());
		Mockito.when(
				this.tickSubscriber.subscribeEquity(trade.getTicker(), org.mockito.Matchers.any(TickObserver.class)))
				.thenReturn(this.handler);

		this.sut.addMonitor(trade);

		final Integer remainingPriceLevels = this.sut.deleteMonitor(trade);
		TickManagerTest.logger.debug("Monitor removed from Tick Manager for Theta: {}", trade);

		MatcherAssert.assertThat(remainingPriceLevels, Matchers.is(Matchers.equalTo(0)));
	}

	@Ignore
	@Test
	public void test_ticks_around_strike_price() {
		final ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		TickManagerTest.logger.debug("Trade initialized: {}", trade);

		final List<ThetaTrade> tradeToReturn = Arrays.asList(trade);
		Mockito.when(this.positonProvider.providePositions(trade.getTicker())).thenReturn(tradeToReturn);
		Mockito.when(
				this.tickSubscriber.subscribeEquity(trade.getTicker(), org.mockito.Matchers.any(TickObserver.class)))
				.thenReturn(this.handler);

		this.sut.addMonitor(trade);

		final ArrayList<Tick> priceTicks = this.generatePriceTicksAround(TickManagerTest.numberOfPriceTicks, trade);

		for (final Tick tick : priceTicks) {
			this.sut.notifyTick(tick.getTicker());
		}

		Mockito.verify(this.executor,
				Mockito.times(this.calculatePriceTransitions(trade.getStrikePrice(),
						priceTicks.stream().map(Tick::getPrice).collect(Collectors.toList()))))
				.reverseTrade(org.mockito.Matchers.any(ThetaTrade.class));
	}
}
