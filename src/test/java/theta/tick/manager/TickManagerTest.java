package theta.tick.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
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
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;

@RunWith(MockitoJUnitRunner.class)
public class TickManagerTest {
	private static final Logger logger = LoggerFactory.getLogger(TickManagerTest.class);

	private static Double aroundPricePlusMinus = 0.05;
	private static Integer numberOfPriceTicks = 10000;

	@Mock
	private TickSubscriber tickSubscriber;
	@Mock
	PositionProvider positonProvider;
	@Mock
	private Executor executor;
	@InjectMocks
	private TickManager sut;

	@Mock
	TickHandler handler;

	@Test
	public void test_add_and_delete_monitor() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		logger.debug("Trade to Monitor: {}", trade);

		Mockito.when(handler.getTicker()).thenReturn(trade.getTicker());
		Mockito.when(this.tickSubscriber.subscribeEquity(trade.getTicker())).thenReturn(handler);

		this.sut.addMonitor(trade);

		TickHandler removedTickHandler = this.sut.deleteMonitor(trade.getTicker());
		logger.debug("Handler removed from Tick Monitor: {}", removedTickHandler);

		assertThat(removedTickHandler.getTicker(), is(equalTo(trade.getTicker())));
	}

	@Test
	public void test_ticks_around_strike_price() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		logger.debug("Trade initialized: {}", trade);

		List<ThetaTrade> tradeToReturn = Arrays.asList(trade);
		Mockito.when(this.positonProvider.providePositions(trade.getTicker())).thenReturn(tradeToReturn);
		Mockito.when(this.tickSubscriber.subscribeEquity(trade.getTicker())).thenReturn(handler);

		this.sut.addMonitor(trade);

		ArrayList<Tick> priceTicks = this.generatePriceTicksAround(TickManagerTest.numberOfPriceTicks, trade);

		for (Tick tick : priceTicks) {
			this.sut.notifyTick(tick);
		}

		Mockito.verify(this.executor,
				Mockito.times(this.calculatePriceTransitions(trade.getStrikePrice(),
						priceTicks.stream().map(Tick::getPrice).collect(Collectors.toList()))))
				.reverseTrade(any(ThetaTrade.class));
	}

	private ArrayList<Tick> generatePriceTicksAround(Integer numberOfTicks, ThetaTrade theta) {
		ArrayList<Tick> priceTicks = new ArrayList<Tick>();

		for (Integer i = 0; i < numberOfTicks; i++) {
			double min = theta.getStrikePrice() - TickManagerTest.aroundPricePlusMinus;
			double max = theta.getStrikePrice() + TickManagerTest.aroundPricePlusMinus;
			double randomAroundPrice = ThreadLocalRandom.current().nextDouble(min, max);
			priceTicks.add(new Tick(theta.getTicker(), Precision.round(randomAroundPrice, 2), TickType.LAST,
					LocalDateTime.now()));
		}

		return priceTicks;
	}

	private Integer calculatePriceTransitions(Double strikePrice, List<Double> list) {
		Integer priceTransitions = 0;
		Boolean isPositionLong = Boolean.TRUE;

		for (Integer i = 0; i < list.size(); i++) {
			Double currentTick = list.get(i);

			if (currentTick > strikePrice && !isPositionLong) {
				priceTransitions++;
				isPositionLong = !isPositionLong;
			} else if (currentTick < strikePrice && isPositionLong) {
				priceTransitions++;
				isPositionLong = !isPositionLong;
			} else {
				logger.error("Possible miscalculated tick");
			}
		}

		return priceTransitions;
	}
}
