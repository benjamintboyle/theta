package theta.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import theta.ThetaEngine;
import theta.managers.strategies.ThetaTradeTest;
import theta.strategies.ThetaTrade;

@RunWith(MockitoJUnitRunner.class)
public class PriceMonitorTest {

	private static Double aroundPricePlusMinus = 0.05;

	@Mock
	private ThetaEngine thetaEngine;

	@InjectMocks
	private PriceMonitor sut = new PriceMonitor(thetaEngine);

	@Test
	public void test_add_and_delete_monitor() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();

		this.sut.addMonitor(trade);

		ThetaTrade removedTrade = this.sut.deleteMonitor(trade.getBackingTicker());

		assertThat(removedTrade, is(equalTo(trade)));
	}

	@Test
	public void test_ticks_around_strike_price() {
		ThetaTrade trade = ThetaTradeTest.buildTestThetaTrade();

		this.sut.addMonitor(trade);

		ArrayList<Double> priceTicks = this.generatePriceTicksAround(10000, trade.getStrikePrice());

		for (Double tick : priceTicks) {
			when(this.thetaEngine.getLast(trade.getBackingTicker())).thenReturn(tick);

			this.sut.notifyPriceChange(trade.getBackingTicker());
		}

		verify(this.thetaEngine, times(this.calculatePriceTransitions(trade.getStrikePrice(), priceTicks)))
				.reverseTrade(trade);
	}

	private ArrayList<Double> generatePriceTicksAround(Integer numberOfTicks, Double price) {
		ArrayList<Double> priceTicks = new ArrayList<Double>();

		for (Integer i = 0; i < numberOfTicks; i++) {
			priceTicks.add(
					ThreadLocalRandom.current().nextDouble(price - aroundPricePlusMinus, price + aroundPricePlusMinus));
		}

		return priceTicks;
	}

	private Integer calculatePriceTransitions(Double strikePrice, ArrayList<Double> priceTicks) {
		Integer priceTransitions = 0;

		Double previousTick = strikePrice;

		for (Integer i = 0; i < priceTicks.size(); i++) {
			Double currentTick = priceTicks.get(i);

			if (currentTick > strikePrice && previousTick < strikePrice) {
				priceTransitions++;
			} else if (currentTick < strikePrice && previousTick > strikePrice) {
				priceTransitions++;
			} else if (previousTick == strikePrice) {
				Integer j = 0;
				while (j < i && previousTick == strikePrice) {
					previousTick = priceTicks.get(i - j);
					j++;
				}
			}
		}

		return priceTransitions;
	}
}
