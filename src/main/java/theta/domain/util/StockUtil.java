package theta.domain.util;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.composed.Theta;
import theta.domain.stock.Stock;

public class StockUtil {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private StockUtil() {}

  // Combine two stocks if everything the same except quantity and average price
  public static Optional<Stock> of(Stock stock1, Stock stock2) {

    Optional<Stock> stock = Optional.empty();

    if (stock1.getId().equals(stock2.getId())) {
      if (stock1.getTicker().equals(stock2.getTicker())) {
        stock = Optional.of(Stock.of(stock1.getId(), stock1.getTicker(), stock1.getQuantity() + stock2.getQuantity(),
            (stock1.getPrice() + stock2.getPrice()) / 2));
      } else {
        logger.warn("Stock Tickers do not match: {} {}", stock1, stock2);
      }
    } else {
      logger.warn("Stock Ids do not match: {} {}", stock1, stock2);
    }

    return stock;
  }

  public static List<Stock> consolidateStock(List<Theta> thetasToConvert) {

    final Map<UUID, Stock> stocksToReverse = new HashMap<>();

    for (final Theta theta : thetasToConvert) {

      if (stocksToReverse.containsKey(theta.getStock().getId())) {

        final Optional<Stock> combinedStock =
            StockUtil.of(stocksToReverse.get(theta.getStock().getId()), theta.getStock());

        if (combinedStock.isPresent()) {
          stocksToReverse.put(theta.getStock().getId(), combinedStock.get());
        } else {
          logger.error("Stock with same Id cannot be combined: {} {}", theta.getStock(),
              stocksToReverse.get(theta.getId()));
        }
      } else {
        stocksToReverse.put(theta.getStock().getId(), theta.getStock());
      }
    }

    return new ArrayList<>(stocksToReverse.values());
  }

  public static Optional<Stock> adjustStockQuantity(Stock stock, long adjustment) {
    final Optional<Security> stockAsSecurity = SecurityUtil.getSecurityWithQuantity(stock, adjustment);

    Optional<Stock> adjustedStock = Optional.empty();

    if (stockAsSecurity.isPresent() && stockAsSecurity.get() instanceof Stock) {
      adjustedStock = Optional.of((Stock) stockAsSecurity.get());
    }

    return adjustedStock;
  }
}
