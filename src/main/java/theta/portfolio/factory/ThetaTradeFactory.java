package theta.portfolio.factory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Option;
import theta.domain.ShortStraddle;
import theta.domain.Stock;
import theta.domain.StockUtil;
import theta.domain.ThetaTrade;

public class ThetaTradeFactory {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static List<ThetaTrade> processThetaTrade(List<Stock> stockList, List<Option> callList, List<Option> putList) {

    final List<ThetaTrade> thetas = new ArrayList<>();

    logger.debug("Processing theta with Stocks: {}, Calls: {}, Puts: {}", stockList, callList, putList);

    final List<ShortStraddle> shortStraddles = ThetaTradeFactory.buildStraddles(callList, putList);

    // For each straddle attempt to add stock portion
    for (final ShortStraddle straddle : shortStraddles) {

      final Optional<Stock> coverableStock = ThetaTradeFactory.getCoverableStock(stockList, straddle);

      // If a stock can cover the straddle
      if (coverableStock.isPresent()) {
        final Stock stock = coverableStock.get();

        // If stock quantity is greater than number of option contracts adjust, otherwise return
        // full stock quantity
        final Stock adjustedStock = StockUtil.adjustStockQuantity(stock, straddle);

        // Build theta
        final Optional<ThetaTrade> theta = ThetaTrade.of(adjustedStock, straddle);

        // Successfully created and added theta to list
        if (theta.isPresent()) {
          thetas.add(theta.get());
        } else {
          logger.warn("ThetaTrade could not be built from Stock: {}, Straddle: {}", adjustedStock, straddle);
        }
      } else {
        logger.warn("No coverable stock could be identified for Straddle: {}, from Stocks: {}", straddle, stockList);
      }
    }

    return thetas;
  }

  private static List<ShortStraddle> buildStraddles(List<Option> calls, List<Option> puts) {

    logger.debug("Building straddles from Calls: {}, Puts: {}", calls, puts);

    final List<ShortStraddle> straddleList = new ArrayList<>();

    for (final Option call : calls) {
      final List<Option> straddlablePuts = puts.stream().filter(put -> put.getExpiration().equals(call.getExpiration()))
          .filter(put -> put.getStrikePrice().equals(call.getStrikePrice())).collect(Collectors.toList());

      if (straddlablePuts.size() > 1) {
        logger.warn("Multiple puts match single call - Call: {}, Puts: {}", call, straddlablePuts);
      }

      final Optional<Option> put = straddlablePuts.stream().findFirst();

      if (put.isPresent()) {
        final Optional<ShortStraddle> straddle = ShortStraddle.of(call, put.get());

        if (straddle.isPresent()) {
          straddleList.add(straddle.get());
        }
      }
    }

    if (straddleList.size() == 0) {
      logger.warn("No Straddles generated by Calls: {}, and Puts: {}", calls, puts);
    }

    return straddleList;
  }

  private static Optional<Stock> getCoverableStock(List<Stock> stockList, ShortStraddle straddle) {
    return stockList.stream().filter(stock -> stock.getTicker().equals(straddle.getTicker()))
        .filter(
            stock -> (Math.abs(stock.getQuantity().intValue()) / 100) >= Math.abs(straddle.getQuantity().intValue()))
        .findFirst();
  }
}
