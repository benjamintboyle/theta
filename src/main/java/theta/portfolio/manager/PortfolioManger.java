package theta.portfolio.manager;

import reactor.core.publisher.Mono;
import theta.api.ManagerController;

public interface PortfolioManger extends ManagerController {
    Mono<Void> startPositionProcessing();
}
