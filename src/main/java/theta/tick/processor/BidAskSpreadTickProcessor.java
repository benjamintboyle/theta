package theta.tick.processor;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.execution.domain.ExecutionType;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.TickType;

public class BidAskSpreadTickProcessor implements TickProcessor {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<Ticker, Double> limitPriceByTicker = new HashMap<>();

  private final Set<TickType> applicableTickTypes = Set.of(TickType.ASK, TickType.BID);

  // Percentage of bid-ask spread before trade occurs. Didn't know where to start, so randomly chose 1
  // standard deviation of normal distribution (68%).
  private static final double DEVIATION = 0.68;
  private static final ExecutionType EXECUTION_TYPE = ExecutionType.LIMIT;

  @Override
  public boolean isApplicable(TickType tickType) {
    return applicableTickTypes.contains(tickType);
  }

  @Override
  public boolean process(Tick tick, PriceLevel priceLevel) {

    boolean shouldReverse = false;

    if (isApplicable(tick.getTickType()) && tick.getAskPrice() > 0 && tick.getBidPrice() > 0) {

      double bidAskSpread = tick.getAskPrice() - tick.getBidPrice();

      double limitPrice = priceLevel.getPrice();

      if (priceLevel.getTicker().equals(tick.getTicker())) {
        if (priceLevel.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
          if (tick.getBidPrice() + (bidAskSpread * DEVIATION) < priceLevel.getPrice()) {
            shouldReverse = true;

            // TODO: Probably just want MARKET order at this point
            if (tick.getAskPrice() < priceLevel.getPrice()) {
              logger.warn("May have been a gap across strike price, Price Level: {}, Tick: {}", priceLevel, tick);
              limitPrice = tick.getBidPrice() + (bidAskSpread * DEVIATION);
            }
          }
        } else if (priceLevel.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
          if (tick.getAskPrice() - (bidAskSpread * DEVIATION) > priceLevel.getPrice()) {
            shouldReverse = true;

            if (tick.getBidPrice() > priceLevel.getPrice()) {
              logger.warn("May have been a gap across strike price, Price Level: {}, Tick: {}", priceLevel, tick);
              limitPrice = tick.getAskPrice() - (bidAskSpread * DEVIATION);
            }
          }
        } else {
          logger.error("Invalid Price Level: {}", priceLevel.tradeIf());
        }
      }


      // FIXME: This is a really terrible implementation.
      Double previousLimit = limitPriceByTicker.put(priceLevel.getTicker(), limitPrice);

      if (previousLimit != null && Double.compare(previousLimit, limitPrice) != 0) {
        logger.error("Processing ticks found different Price Levels: {} and {} for {}", previousLimit, limitPrice,
            priceLevel);
      }
    }

    return shouldReverse;
  }

  @Override
  public ExecutionType getExecutionType() {
    return EXECUTION_TYPE;
  }

  // FIXME: This is a really terrible implementation.
  @Override
  public Optional<Double> getLimitPrice(Ticker ticker) {
    return Optional.of(limitPriceByTicker.get(ticker));
  }

}
