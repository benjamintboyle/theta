package theta;

import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.Security;
import theta.api.TickSubscriber;
import theta.domain.ThetaTrade;
import theta.domain.ThetaTradeTest;
import theta.execution.api.Executable;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationTest {

	@Mock
	private PositionHandler mockPositionHandler;
	@Mock
	private TickSubscriber mockTickSubscriber;
	@Mock
	private ExecutionHandler mockExecutionHandler;

	private PortfolioManager spyPortfolioManager;
	private TickManager spyTickManager;
	private ExecutionManager spyExecutionManager;

	@Before
	public void setUp() {
		this.spyExecutionManager = Mockito.spy(new ExecutionManager(mockExecutionHandler));
		this.spyTickManager = Mockito.spy(new TickManager(mockTickSubscriber));
		this.spyPortfolioManager = Mockito.spy(new PortfolioManager(mockPositionHandler));

		this.spyPortfolioManager.registerTickMonitor(spyTickManager);
		this.spyTickManager.registerExecutor(spyExecutionManager);
		this.spyTickManager.registerPositionProvider(spyPortfolioManager);
	}

	@Test
	public void IntegrationTest_OnePositionOneContract() {
		ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

		// Add all securities to be ingested to a list
		List<Security> listOfSecurities = thetaTrade.toSecurityList();

		// Send all securities in list for ingestion
		this.sendPositionListForIngestion(listOfSecurities);

		List<ThetaTrade> thetaList = new ArrayList<ThetaTrade>();
		thetaList.add(thetaTrade);
		List<Tick> tickList = generateTickList(thetaList);

		this.sendTickListForIngestion(tickList);

		Mockito.verify(this.mockExecutionHandler, Mockito.times(6)).executeOrder(Mockito.any(Executable.class));
	}

	@Test
	public void integrationTest_onePositionThreeContracts() {
		// assertThat();
		fail("Not yet implemented");
	}

	@Test
	public void integrationTest_threePositionsOneContract() {
		// assertThat();
		fail("Not yet implemented");
	}

	@Test
	public void integrationTest_threePositionsThreeContracts() {
		// assertThat();
		fail("Not yet implemented");
	}

	@Test
	public void integrationTest_onePositionThreeStrikePrices() {
		// assertThat();
		fail("Not yet implemented");
	}

	private void sendPositionListForIngestion(List<Security> positions) {
		positions.forEach(this.spyPortfolioManager::ingestPosition);
	}

	private void sendTickListForIngestion(List<Tick> tickList) {
		tickList.stream().forEach(tick -> this.spyTickManager.notifyTick(tick));
	}

	private List<Tick> generateTickList(List<ThetaTrade> thetaList) {
		List<Tick> returnTicks = new ArrayList<Tick>();
		List<Tick> zeroTicks = new ArrayList<Tick>();

		zeroTicks = generateZeroTickList(thetaList);
		returnTicks = generateStandardTickPatternList(zeroTicks);

		return returnTicks;
	}

	private List<Tick> generateStandardTickPatternList(List<Tick> zeroTicks) {
		List<Tick> tickList = new ArrayList<Tick>();
		TickType tickType = TickType.LAST;

		for (Tick tick : zeroTicks) {
			String ticker = tick.getTicker();
			Double strike = tick.getPrice();

			tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike - 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike - 0.01, tickType, LocalDateTime.now()));
			/*
			 * tickList.add(new Tick(ticker, strike, tickType,
			 * LocalDateTime.now())); tickList.add(new Tick(ticker, strike,
			 * tickType, LocalDateTime.now())); tickList.add(new Tick(ticker,
			 * strike + 0.01, tickType, LocalDateTime.now())); tickList.add(new
			 * Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			 * tickList.add(new Tick(ticker, strike - 0.01, tickType,
			 * LocalDateTime.now())); tickList.add(new Tick(ticker, strike -
			 * 0.01, tickType, LocalDateTime.now())); tickList.add(new
			 * Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			 * tickList.add(new Tick(ticker, strike + 0.01, tickType,
			 * LocalDateTime.now())); tickList.add(new Tick(ticker, strike -
			 * 0.01, tickType, LocalDateTime.now())); tickList.add(new
			 * Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			 */
		}

		return tickList;
	}

	private List<Tick> generateZeroTickList(List<ThetaTrade> thetaList) {
		List<Tick> zeroTickList = new ArrayList<Tick>();

		for (ThetaTrade theta : thetaList) {
			zeroTickList.add(
					new Tick(theta.getBackingTicker(), theta.getStrikePrice(), TickType.LAST, LocalDateTime.now()));
		}
		return zeroTickList;
	}
}
