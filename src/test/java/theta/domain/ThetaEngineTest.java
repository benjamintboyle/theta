package theta.domain;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import brokers.interactive_brokers.IbPositionHandler;
import brokers.interactive_brokers.IbTickHandler;
import theta.api.Security;
import theta.connection.manager.ConnectionManager;
import theta.portfolio.manager.PortfolioManagerTest;

@RunWith(MockitoJUnitRunner.class)
public class ThetaEngineTest {

	@Mock
	ConnectionManager connectionManager;
	@InjectMocks
	ThetaEngine sut;

	IbPositionHandler positionHandler = sut.getPositionHandler();
	Map<String, IbTickHandler> tickHandlers = sut.getTickHandlers();

	@Test
	public void FullIntegrationTest() {
		// this.loadPositionsFromFile(positionFile);
		fail("Not yet implemented");
	}

	private void loadPositionsFromFile(String positionFile) {
		for (Security security : PortfolioManagerTest.readInputFile(positionFile)) {

		}
	}
}
