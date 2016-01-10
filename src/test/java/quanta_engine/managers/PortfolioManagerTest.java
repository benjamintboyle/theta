package quanta_engine.managers;

import java.io.IOException;
import java.net.URISyntaxException;
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

import quanta_engine.QuantaEngine;
import quanta_engine.strategies.ExtrinsicCapture;
import quanta_engine.strategies.Option;
import quanta_engine.strategies.Stock;
import quanta_engine.strategies.api.SecurityType;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioManagerTest {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManagerTest.class);

	@Mock
	private QuantaEngine qeMock;
	@Mock
	private PriceMonitor pmMock;
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
		List<String> trades = readInputFiles(filename);

		for (String trade : trades) {
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
				sut.ingestPosition(stock);
				break;
			case "CALL":
				Option call = new Option(SecurityType.CALL, ticker, quantity, price,
						LocalDate.now().plusDays(Long.valueOf(security[4])));
				logger.debug("Sending Call: {}", call);

				sut.ingestPosition(call);
				break;
			case "PUT":
				Option put = new Option(SecurityType.PUT, ticker, quantity, price,
						LocalDate.now().plusDays(Long.valueOf(security[4])));
				logger.debug("Sending Put: {}", put);
				sut.ingestPosition(put);
				break;
			}
		}

		Mockito.verify(pmMock, Mockito.times(6)).addMonitor(Mockito.any(ExtrinsicCapture.class));

		/*
		 * @Captor // private ArgumentCaptor<ExtrinsicCapture> captor;
		 * 
		 * Mockito.verify(pmMock,
		 * Mockito.times(6)).addMonitor(captor.capture());
		 * 
		 * 
		 * assertEquals(Arrays.asList(new Data("a"), new Data("b"), new
		 * Data("c")), captor.getAllValues());
		 */
	}

	private List<String> readInputFiles(String fileName) {
		List<String> inputList = new ArrayList<String>();

		try {
			Path inputFile = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());

			Stream<String> stream = Files.lines(inputFile);
			inputList = stream.collect(Collectors.toList());
			stream.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return inputList;
	}
}
