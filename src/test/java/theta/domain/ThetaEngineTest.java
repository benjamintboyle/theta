package theta.domain;

import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import theta.ThetaEngine;
import theta.api.Security;
import theta.connection.manager.ConnectionManager;
import theta.execution.api.Executable;
import theta.execution.manager.ExecutionManager;
import theta.execution.manager.ExecutionManagerTest;
import theta.portfolio.manager.PortfolioManager;
import theta.portfolio.manager.PortfolioManagerTest;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.manager.TickManager;
import theta.tick.manager.TickManagerTest;

@RunWith(MockitoJUnitRunner.class)
public class ThetaEngineTest {

	@Mock
	private ConnectionManager connectionMock;
	@Spy
	private ExecutionManager executionSpy = ExecutionManagerTest.buildExecutionManager(connectionMock);
	@Spy
	private TickManager tickSpy = TickManagerTest.buildTickManager(connectionMock, executionSpy);
	@Spy
	private PortfolioManager portfolioSpy = PortfolioManagerTest.buildPortfolioManager(connectionMock, tickSpy);

	private ThetaEngine sut = new ThetaEngine();

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

		Mockito.verify(this.executionSpy, Mockito.times(6)).execute(Mockito.any(Executable.class));
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
		positions.forEach(security -> this.sut.getPortfolioReceiver().ingestPosition(security));

	}

	private void sendTickListForIngestion(List<Tick> tickList) {
		tickList.stream().forEach(tick -> this.sut.getTickReceiver().notifyTick(tick));
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
			tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike - 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike - 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike - 0.01, tickType, LocalDateTime.now()));
			tickList.add(new Tick(ticker, strike + 0.01, tickType, LocalDateTime.now()));
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
