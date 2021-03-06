package theta.tick.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutionType;
import theta.execution.domain.CandidateStockOrder;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.TickType;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

// TODO: This whole class needs to be fixed to process more straightforwardly
@Component
public class LastTickProcessor implements TickProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Set<TickType> applicableTickTypes = Set.of(TickType.LAST);
    private static final ExecutionType EXECUTION_TYPE = ExecutionType.MARKET;

    @Override
    public boolean isApplicable(TickType tickType) {
        return applicableTickTypes.contains(tickType);
    }

    @Override
    public boolean processTick(Tick tick, PriceLevel priceLevel) {
        boolean shouldReverse = false;

        if (isApplicable(tick.getTickType())
                && tick.getLastPrice() > 0
                && priceLevel.getTicker().equals(tick.getTicker())) {
            logger.debug("Checking {} against Price Level: {}", tick, priceLevel);

            if (priceLevel.tradeIf() == PriceLevelDirection.FALLS_BELOW) {
                if (tick.getLastPrice() < priceLevel.getPrice()) {
                    shouldReverse = true;
                }
            } else if (priceLevel.tradeIf() == PriceLevelDirection.RISES_ABOVE) {
                if (tick.getLastPrice() > priceLevel.getPrice()) {
                    shouldReverse = true;
                }
            } else {
                logger.error("Invalid Price Level: {}", priceLevel.tradeIf());
            }
        }

        return shouldReverse;
    }

    @Override
    public CandidateStockOrder getCandidateStockOrder(Stock stock) {
        return new CandidateStockOrder(stock, EXECUTION_TYPE, Optional.empty());
    }
}
