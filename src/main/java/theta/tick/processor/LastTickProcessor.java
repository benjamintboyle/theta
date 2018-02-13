package theta.tick.processor;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.execution.api.ExecutionType;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.TickType;

// TODO: This whole class needs to be fixed to process more straightforwardly
public class LastTickProcessor implements TickProcessor {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Set<TickType> applicableTickTypes = Set.of(TickType.LAST);
  private static final ExecutionType EXECUTION_TYPE = ExecutionType.MARKET;

  @Override
  public boolean isApplicable(TickType tickType) {
    return applicableTickTypes.contains(tickType);
  }

  @Override
  public boolean process(Tick tick, PriceLevel priceLevel) {

    boolean shouldReverse = false;

    if (isApplicable(tick.getTickType()) && tick.getLastPrice() > 0) {

      logger.debug("Checking {} against Price Level: {}", tick, priceLevel);

      if (priceLevel.getTicker().equals(tick.getTicker())) {
        if (priceLevel.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
          if (tick.getLastPrice() < priceLevel.getPrice()) {
            shouldReverse = true;
          }
        } else if (priceLevel.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
          if (tick.getLastPrice() > priceLevel.getPrice()) {
            shouldReverse = true;
          }
        } else {
          logger.error("Invalid Price Level: {}", priceLevel.tradeIf());
        }
      }
    }

    return shouldReverse;
  }

  @Override
  public ExecutionType getExecutionType() {
    return EXECUTION_TYPE;
  }

  @Override
  public Optional<Double> getLimitPrice(Ticker ticker) {
    return Optional.empty();
  }

}
