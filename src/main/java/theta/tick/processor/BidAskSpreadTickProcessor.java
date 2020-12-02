package theta.tick.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutionType;
import theta.execution.domain.CandidateStockOrder;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.TickType;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BidAskSpreadTickProcessor implements TickProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Percentage of bid-ask spread before trade occurs. Didn't know where to start,
    //   so randomly chose 1 standard deviation of normal distribution (68%).
    private static final double DEVIATION = 0.68;
    private static final ExecutionType EXECUTION_TYPE = ExecutionType.LIMIT;

    private final Map<Ticker, Double> limitPriceByTicker = new HashMap<>();
    private final Set<TickType> applicableTickTypes = Set.of(TickType.ASK, TickType.BID);

    @Override
    public boolean isApplicable(TickType tickType) {
        return applicableTickTypes.contains(tickType);
    }

    @Override
    public boolean processTick(Tick tick, PriceLevel priceLevel) {
        boolean shouldReverse = false;

        if (isApplicable(tick.getTickType()) && tick.getAskPrice() > 0 && tick.getBidPrice() > 0 && priceLevel.getTicker().equals(tick.getTicker())) {
            double limitPrice = priceLevel.getPrice();

            final double bidAskSpreadDeviation = switch (priceLevel.tradeIf()) {
                case FALLS_BELOW -> tick.getBidPrice() + (tick.getAskPrice() - tick.getBidPrice()) * DEVIATION;
                case RISES_ABOVE -> tick.getAskPrice() - (tick.getAskPrice() - tick.getBidPrice()) * DEVIATION;
            };

            switch (priceLevel.tradeIf()) {
                case FALLS_BELOW -> {
                    if (bidAskSpreadDeviation < priceLevel.getPrice()) {
                        shouldReverse = true;

                        // TODO: Probably just want MARKET order at this point
                        if (tick.getAskPrice() < priceLevel.getPrice()) {
                            logWarning(priceLevel, tick);
                            limitPrice = bidAskSpreadDeviation;
                        }
                    }
                }
                case RISES_ABOVE -> {
                    if (bidAskSpreadDeviation > priceLevel.getPrice()) {
                        shouldReverse = true;

                        if (tick.getBidPrice() > priceLevel.getPrice()) {
                            logWarning(priceLevel, tick);
                            limitPrice = bidAskSpreadDeviation;
                        }
                    }
                }
            }

            final Double previousLimit = limitPriceByTicker.put(priceLevel.getTicker(), Math.round(limitPrice * 100.0) / 100.0);

            if (previousLimit != null && Double.compare(previousLimit, limitPriceByTicker.get(priceLevel.getTicker())) != 0) {
                logger.warn("Processing ticks found different Price Levels: {} and {} for Price Level: {}", limitPriceByTicker.get(priceLevel.getTicker()), previousLimit, priceLevel);
            }
        }

        return shouldReverse;
    }

    @Override
    public CandidateStockOrder getCandidateStockOrder(Stock stock) {
        if (limitPriceByTicker.containsKey(stock.getTicker())) {
            return new CandidateStockOrder(stock, EXECUTION_TYPE, Optional.of(limitPriceByTicker.get(stock.getTicker())));
        } else {
            logger.warn("Performing Market order, instead of Limit. No limit price available for {}", stock.getTicker());
            return new CandidateStockOrder(stock, ExecutionType.MARKET, Optional.empty());
        }
    }

    private void logWarning(PriceLevel priceLevel, Tick tick) {
        logger.warn("May have been a gap across strike price, Price Level: {}, Tick: {}", priceLevel, tick);
    }
}
