package theta.portfolio.manager;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class PortfolioTestUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    return PortfolioTestUtil.parseStringToSecurity(inputList);
  }

  private static List<Security> parseStringToSecurity(final List<String> stringListOfSecurities) {
    final List<Security> securityList = new ArrayList<Security>();

    final Pattern splitPattern = Pattern.compile("\\s*,\\s*");

    for (final String trade : stringListOfSecurities) {
      logger.debug("Trade: {}", trade);

      final String[] security = splitPattern.split(trade);
      final String securityType = security[0];
      final String ticker = security[1];
      final Double quantity = Double.valueOf(security[2]);
      final Double price = Double.valueOf(security[3]);
      logger.debug("Type: {}, Ticker: {}, Quantity: {}, Price: {}", securityType, ticker, quantity, price);

      switch (securityType) {
        case "STOCK":
          final Stock stock = Stock.of(ticker, quantity, price);
          logger.debug("Sending Stock: {}", stock);
          securityList.add(stock);
          break;
        case "CALL":
          final Option call = new Option(UUID.randomUUID(), SecurityType.CALL, ticker, quantity, price,
              LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
          logger.debug("Sending Call: {}", call);

          securityList.add(call);
          break;
        case "PUT":
          final Option put = new Option(UUID.randomUUID(), SecurityType.PUT, ticker, quantity, price,
              LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
          logger.debug("Sending Put: {}", put);
          securityList.add(put);
          break;
        default:
          logger.error("Could not determine SecurityType: {}", securityType);
      }
    }

    return securityList;
  }
}
