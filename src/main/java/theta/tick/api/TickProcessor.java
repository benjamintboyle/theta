package theta.tick.api;

import theta.domain.PriceLevel;
import theta.domain.stock.Stock;
import theta.execution.domain.CandidateStockOrder;
import theta.tick.domain.TickType;

public interface TickProcessor {
    boolean isApplicable(TickType tickType);

    boolean processTick(Tick tick, PriceLevel priceLevel);

    CandidateStockOrder getCandidateStockOrder(Stock stock);
}
