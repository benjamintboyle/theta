package theta.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.ThetaEngine;
import theta.managers.strategies.ThetaTradeTest;
import theta.strategies.ThetaTrade;

@RunWith(MockitoJUnitRunner.class)
public class PriceMonitorTest {
	private final Logger logger = LoggerFactory.getLogger(PriceMonitorTest.class);

	private static Double aroundPricePlusMinus = 0.05;
	private static Integer numberOfPriceTicks = 10000;

	@Mock
	private ThetaEngine thetaEngine;

	@InjectMocks
	private PriceMonitor sut = new PriceMonitor(thetaEngine);

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

		ArrayList<Double> priceTicks = this.generatePriceTicksAround(PriceMonitorTest.numberOfPriceTicks,
				trade.getStrikePrice());

		for (Double tick : priceTicks) {
			when(this.thetaEngine.getLast(trade.getBackingTicker())).thenReturn(tick);
			this.sut.notifyPriceChange(trade.getBackingTicker());
		}

		verify(this.thetaEngine, times(this.calculatePriceTransitions(trade.getStrikePrice(), priceTicks)))
				.reverseTrade(any(ThetaTrade.class));
	}

	private ArrayList<Double> generatePriceTicksAround(Integer numberOfTicks, Double price) {
		ArrayList<Double> priceTicks = new ArrayList<Double>();

		for (Integer i = 0; i < numberOfTicks; i++) {
			double min = price - PriceMonitorTest.aroundPricePlusMinus;
			double max = price + PriceMonitorTest.aroundPricePlusMinus;
			double randomAroundPrice = ThreadLocalRandom.current().nextDouble(min, max);
			priceTicks.add(Precision.round(randomAroundPrice, 2));
		}

		return priceTicks;
	}

	private Integer calculatePriceTransitions(Double strikePrice, ArrayList<Double> priceTicks) {
		Integer priceTransitions = 0;
		Boolean isPositionLong = Boolean.TRUE;

		for (Integer i = 0; i < priceTicks.size(); i++) {
			Double currentTick = priceTicks.get(i);

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
