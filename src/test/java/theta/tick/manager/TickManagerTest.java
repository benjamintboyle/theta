package theta.tick.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.connection.api.Controller;
import theta.connection.manager.ConnectionManager;
import theta.domain.ThetaTrade;
import theta.domain.ThetaTradeTest;
import theta.execution.api.Executor;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.class)
public class TickManagerTest {
	private final Logger logger = LoggerFactory.getLogger(TickManagerTest.class);

	private static Double aroundPricePlusMinus = 0.05;
	private static Integer numberOfPriceTicks = 10000;

	@Mock
	private Controller controllor;
	@Mock
	private Executor executor;
	@InjectMocks
	private TickManager sut;

	public static TickManager buildTickManager(ConnectionManager connectionManager, ExecutionManager executionManager) {
		return new TickManager(connectionManager, executionManager);
	}

	@Test
	public void test_add_and_delete_monitor() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		this.logger.debug("Trade to Monitor: {}", trade);

		this.sut.addMonitor(trade);

		ThetaTrade removedTrade = this.sut.deleteMonitor(trade.getBackingTicker());
		this.logger.debug("Trade removed from Monitor: {}", trade);

		assertThat(removedTrade, is(equalTo(trade)));
	}

	@Test
	public void test_ticks_around_strike_price() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();
		this.logger.debug("Trade initialized: {}", trade);

		this.sut.addMonitor(trade);

		ArrayList<Tick> priceTicks = this.generatePriceTicksAround(TickManagerTest.numberOfPriceTicks, trade);

		for (Tick tick : priceTicks) {
			this.sut.notifyTick(tick);
		}

		verify(this.executor,
				times(this.calculatePriceTransitions(trade.getStrikePrice(),
						priceTicks.stream().map(Tick::getPrice).collect(Collectors.toList()))))
								.reverseTrade(any(ThetaTrade.class));
	}

	private ArrayList<Tick> generatePriceTicksAround(Integer numberOfTicks, ThetaTrade theta) {
		ArrayList<Tick> priceTicks = new ArrayList<Tick>();

		for (Integer i = 0; i < numberOfTicks; i++) {
			double min = theta.getStrikePrice() - TickManagerTest.aroundPricePlusMinus;
			double max = theta.getStrikePrice() + TickManagerTest.aroundPricePlusMinus;
			double randomAroundPrice = ThreadLocalRandom.current().nextDouble(min, max);
			priceTicks.add(new Tick(theta.getBackingTicker(), Precision.round(randomAroundPrice, 2), TickType.LAST));
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
				this.logger.error("Possible miscalculated tick");
			}
		}

		return priceTransitions;
	}
}
