package theta.tick.processor;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.execution.domain.ExecutionType;
import theta.tick.api.Tick;
import theta.tick.domain.TickType;

public class BidAskSpreadTickProcessor implements TickProcessor {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Optional<Double> limitPrice = Optional.empty();

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

    if (isApplicable(tick.getTickType())) {

      double bidAskSpread = tick.getAskPrice() - tick.getBidPrice();

      logger.debug("Checking Tick {} against Price Level: {}", tick, priceLevel);

      if (priceLevel.getTicker().equals(tick.getTicker())) {
        if (priceLevel.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
          if (tick.getBidPrice() + (bidAskSpread * DEVIATION) < priceLevel.getPrice()) {
            shouldReverse = true;
          } else {
            logger.error("Unexecuted - PriceLevel: {}, Tick: {}", priceLevel, tick);
          }
        } else if (priceLevel.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
          if (tick.getAskPrice() - (bidAskSpread * DEVIATION) > priceLevel.getPrice()) {
            shouldReverse = true;
          } else {
            logger.error("Unexecuted - PriceLevel: {}, Tick: {}", priceLevel, tick);
          }
        } else {
          logger.error("Invalid Price Level: {}", priceLevel.tradeIf());
        }
      }


      if (!limitPrice.isPresent()) {
        limitPrice = Optional.of(priceLevel.getPrice());
      } else if (Double.compare(limitPrice.get(), priceLevel.getPrice()) != 0) {
        logger.error("Processing ticks found different strike prices {} and {}", priceLevel.getPrice(),
            limitPrice.get());
      }
    }

    return shouldReverse;
  }

  @Override
  public ExecutionType getExecutionType() {
    return EXECUTION_TYPE;
  }

  @Override
  public Optional<Double> getLimitPrice() {
    return limitPrice;
  }

}
