package theta.portfolio.manager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.NewContract;

import theta.api.Security;
import theta.api.SecurityType;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaEngine;
import theta.domain.ThetaTrade;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioManagerTest {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManagerTest.class);

	@Mock
	private ThetaEngine qeMock;
	@Mock
	private TickManager pmMock;
	@InjectMocks
	private PortfolioManager sut;

	@Test
	public void ingest_trades_in_order() {
		this.ingest_test("load_trades_in_order.txt");
	}

	@Test
	public void ingest_trades_out_of_order() {
		this.ingest_test("load_trades_out_of_order.txt");
	}

	private void ingest_test(String filename) {
		List<Security> securitiesList = readInputFile(filename);

		for (Security security : securitiesList) {
			logger.debug("Trade: {}", security);

			sut.ingestPosition(security);
		}

		Mockito.verify(pmMock, Mockito.times(6)).addMonitor(Mockito.any(ThetaTrade.class));
	}

	public static List<Security> readInputFile(String fileName) {
		List<String> inputList = new ArrayList<String>();

		try {
			Path inputFile = Paths.get(PortfolioManagerTest.class.getClassLoader().getResource(fileName).toURI());

			Stream<String> stream = Files.lines(inputFile);
			inputList = stream.collect(Collectors.toList());
			stream.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return parseStringToSecurity(inputList);
	}

	private static List<Security> parseStringToSecurity(List<String> stringListOfSecurities) {
		List<Security> securityList = new ArrayList<Security>();

		for (String trade : stringListOfSecurities) {
			logger.debug("Trade: {}", trade);

			String[] security = trade.split("\\s*,\\s*");
			String securityType = security[0];
			String ticker = security[1];
			Integer quantity = Integer.valueOf(security[2]);
			Double price = Double.valueOf(security[3]);
			logger.debug("Type: {}, Ticker: {}, Quantity: {}, Price: {}", securityType, ticker, quantity, price);

			switch (securityType) {
			case "STOCK":
				Stock stock = new Stock(ticker, quantity, price, new NewContract());
				logger.debug("Sending Stock: {}", stock);
				securityList.add(stock);
				break;
			case "CALL":
				Option call = new Option(SecurityType.CALL, ticker, quantity, price,
						LocalDate.now().plusDays(Long.valueOf(security[4])));
				logger.debug("Sending Call: {}", call);

				securityList.add(call);
				break;
			case "PUT":
				Option put = new Option(SecurityType.PUT, ticker, quantity, price,
						LocalDate.now().plusDays(Long.valueOf(security[4])));
				logger.debug("Sending Put: {}", put);
				securityList.add(put);
				break;
			}
		}

		return securityList;
	}
}