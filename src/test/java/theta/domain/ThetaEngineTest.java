package theta.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import theta.api.Security;
import theta.connection.manager.ConnectionManager;
import theta.portfolio.api.PortfolioReceiver;
import theta.portfolio.manager.PortfolioManagerTest;
import theta.tick.api.TickReceiver;

@RunWith(MockitoJUnitRunner.class)
public class ThetaEngineTest {

	@Mock
	private ConnectionManager connectionManager;
	@InjectMocks
	private ThetaEngine sut;

	private PortfolioReceiver portfolioReceiver = sut.getPortfolioReceiver();
	private TickReceiver tickReceiver = sut.getTickReceiver();

	@Test
	public void FullIntegrationTest() {
		this.loadPositionsFromFile("theta_engine_test__full_integration_test.txt");

		// ingest ticks

		// validate executions
		fail("Not yet implemented");
	}

	private void loadPositionsFromFile(String positionFile) {
		for (Security security : PortfolioManagerTest.readInputFile(positionFile)) {
			this.portfolioReceiver.ingestPosition(security);
		}
	}

	private void ingestTicksFromFile(String tickFile) {

	}
}
