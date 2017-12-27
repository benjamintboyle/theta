package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  public static List<Stock> consolidateStock(List<ThetaTrade> thetasToConvert) {

    final Map<UUID, Stock> thetasToReverse = new HashMap<>();

    for (final ThetaTrade theta : thetasToConvert) {
      if (thetasToReverse.containsKey(theta.getStock().getId())) {
        final Optional<Stock> combinedStock =
            StockUtil.of(thetasToReverse.get(theta.getStock().getId()), theta.getStock());

        if (combinedStock.isPresent()) {
          thetasToReverse.put(theta.getStock().getId(), combinedStock.get());
        } else {
          logger.error("Stock with same Id cannot be combined: {} {}", theta.getStock(),
              thetasToReverse.get(theta.getId()));
        }
      } else {
        thetasToReverse.put(theta.getStock().getId(), theta.getStock());
      }
    }

    return new ArrayList<>(thetasToReverse.values());
  }

  public static Stock adjustStockQuantity(Stock stock, ShortStraddle straddle) {
    Stock adjustedStock = stock;

    // If there is an exact quantity match for stocks
    if (Math.abs(stock.getQuantity().intValue()) / 100 > Math.abs(straddle.getQuantity().intValue())) {
      // If not exact match for quantity, then use only part of it to cover
      final Double quantity =
          stock.getQuantity() / Math.abs(stock.getQuantity()) * Math.abs(straddle.getQuantity()) * 100;
      adjustedStock = Stock.of(stock.getId(), stock.getTicker(), quantity, stock.getPrice());
    }

    logger.debug("Stock adjusted from Initial: {}, to Adjusted: {}, based on Straddle: {}", stock, adjustedStock,
        straddle);

    return adjustedStock;
  }

}
