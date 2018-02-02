package theta.tick.processor;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.DefaultPriceLevel;
import theta.domain.Theta;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.tick.api.Tick;

// TODO: This whole class needs to be fixed to process more straightforwardly
public class TickProcessor implements Function<Theta, List<Theta>> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Tick tick;

  public TickProcessor(Tick tick) {
    this.tick = tick;
  }

  @Override
  public List<Theta> apply(Theta theta) {
    return processTick(theta);
  }

  private List<Theta> processTick(Theta theta) {

    final PriceLevel priceLevel = DefaultPriceLevel.of(theta);
    final List<Theta> tradesToReverse = new ArrayList<>();

    logger.info("Checking Tick against position: {}", theta.toString());

    if (priceLevel.getTicker().equals(tick.getTicker())) {
      if (priceLevel.tradeIf().equals(PriceLevelDirection.FALLS_BELOW)) {
        if (tick.getLastPrice() < priceLevel.getStrikePrice()) {
          tradesToReverse.add(theta);
        } else {
          logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}", PriceLevelDirection.FALLS_BELOW, tick,
              theta);
        }
      } else if (priceLevel.tradeIf().equals(PriceLevelDirection.RISES_ABOVE)) {
        if (tick.getLastPrice() > priceLevel.getStrikePrice()) {
          tradesToReverse.add(theta);
        } else {
          logger.error("Unexecuted - PriceLevel: {}, Tick: {}, Theta: {}", PriceLevelDirection.RISES_ABOVE, tick,
              theta);
        }
      } else {
        logger.error("Invalid Price Level: {}", priceLevel.tradeIf());
      }
    }

    return tradesToReverse;
  }

}
