package theta.execution.api;

import reactor.core.publisher.Mono;
import theta.api.ManagerController;
import theta.domain.Ticker;
import theta.execution.domain.CandidateStockOrder;

public interface Executor extends ManagerController {
    Mono<Void> reverseTrade(CandidateStockOrder candidateOrder);

    void convertToMarketOrderIfExists(Ticker ticker);
}
