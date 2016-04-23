package theta.portfolio.manager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioManagerTest {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManagerTest.class);

	private static List<Security> parseStringToSecurity(final List<String> stringListOfSecurities) {
		final List<Security> securityList = new ArrayList<Security>();

		final Pattern splitPattern = Pattern.compile("\\s*,\\s*");

		for (final String trade : stringListOfSecurities) {
			PortfolioManagerTest.logger.debug("Trade: {}", trade);

			final String[] security = splitPattern.split(trade);
			final String securityType = security[0];
			final String ticker = security[1];
			final Integer quantity = Integer.valueOf(security[2]);
			final Double price = Double.valueOf(security[3]);
			PortfolioManagerTest.logger.debug("Type: {}, Ticker: {}, Quantity: {}, Price: {}", securityType, ticker,
					quantity, price);

			switch (securityType) {
			case "STOCK":
				final Stock stock = new Stock(UUID.randomUUID(), ticker, quantity, price);
				PortfolioManagerTest.logger.debug("Sending Stock: {}", stock);
				securityList.add(stock);
				break;
			case "CALL":
				final Option call = new Option(UUID.randomUUID(), SecurityType.CALL, ticker, quantity, price,
						LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
				PortfolioManagerTest.logger.debug("Sending Call: {}", call);

				securityList.add(call);
				break;
			case "PUT":
				final Option put = new Option(UUID.randomUUID(), SecurityType.PUT, ticker, quantity, price,
						LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
				PortfolioManagerTest.logger.debug("Sending Put: {}", put);
				securityList.add(put);
				break;
			default:
				PortfolioManagerTest.logger.error("Could not determine SecurityType: {}", securityType);
			}
		}

		return securityList;
	}

	public static List<Security> readInputFile(final String fileName) {
		List<String> inputList = new ArrayList<String>();

		try {
			final Path inputFile = Paths.get(PortfolioManagerTest.class.getClassLoader().getResource(fileName).toURI());

			final Stream<String> stream = Files.lines(inputFile);
			inputList = stream.collect(Collectors.toList());
			stream.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return PortfolioManagerTest.parseStringToSecurity(inputList);
	}

	@Mock
	private TickManager pmMock;

	@Mock
	private PositionHandler positionHandlerMock;

	@InjectMocks
	private PortfolioManager sut;

	private void ingest_test(final String filename) {
		final List<Security> securitiesList = PortfolioManagerTest.readInputFile(filename);

		for (final Security security : securitiesList) {
			PortfolioManagerTest.logger.debug("Trade: {}", security);

			this.sut.ingestPosition(security);
		}

		Mockito.verify(this.pmMock, Mockito.times(6)).addMonitor(Matchers.any(ThetaTrade.class));
	}

	@Ignore
	@Test
	public void ingest_trades_in_order() {
		this.ingest_test("load_trades_in_order.txt");
	}

	@Ignore
	@Test
	public void ingest_trades_out_of_order() {
		this.ingest_test("load_trades_out_of_order.txt");
	}
}
