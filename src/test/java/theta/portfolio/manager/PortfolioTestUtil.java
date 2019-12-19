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
import lombok.extern.slf4j.Slf4j;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

@Slf4j
public class PortfolioTestUtil {

  /**
   * Read file containing test data of securities (stocks).
   *
   * @param fileName Filename for test data
   * @return List of Securities (stocks) from file
   */
  public static List<Security> readInputFile(final String fileName) {
    List<String> inputList = new ArrayList<>();

    try {
      final Path inputFile =
          Paths.get(PortfolioManagerTest.class.getClassLoader().getResource(fileName).toURI());

      final Stream<String> stream = Files.lines(inputFile);
      inputList = stream.collect(Collectors.toList());
      stream.close();
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }

    return PortfolioTestUtil.parseStringToSecurity(inputList);
  }

  /**
   * Parses line of test data into Security.
   *
   * @param stringListOfSecurities String of security data (typically from file)
   * @return List of parsed securities
   */
  private static List<Security> parseStringToSecurity(final List<String> stringListOfSecurities) {
    final List<Security> securityList = new ArrayList<>();

    final Pattern splitPattern = Pattern.compile("\\s*,\\s*");

    for (final String trade : stringListOfSecurities) {
      log.debug("Trade: {}", trade);

      final String[] security = splitPattern.split(trade);
      final String securityType = security[0];
      final Ticker ticker = DefaultTicker.from(security[1]);
      final long quantity = Long.parseLong(security[2]);
      final Double price = Double.valueOf(security[3]);
      log.debug("Type: {}, Ticker: {}, Quantity: {}, Price: {}", securityType, ticker, quantity,
          price);

      switch (securityType) {
        case "STOCK":
          final Stock stock = Stock.of(ticker, quantity, price);
          log.debug("Sending Stock: {}", stock);
          securityList.add(stock);
          break;
        case "CALL":
          final Option call = new Option(UUID.randomUUID(), SecurityType.CALL, ticker, quantity,
              price, LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
          log.debug("Sending Call: {}", call);

          securityList.add(call);
          break;
        case "PUT":
          final Option put = new Option(UUID.randomUUID(), SecurityType.PUT, ticker, quantity,
              price, LocalDate.now().plusDays(Long.parseLong(security[4])), 0.0);
          log.debug("Sending Put: {}", put);
          securityList.add(put);
          break;
        default:
          log.error("Could not determine SecurityType: {}", securityType);
      }
    }

    return securityList;
  }

}
