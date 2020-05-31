package theta.api;

import reactor.core.publisher.Flux;
import theta.domain.Security;

public interface PositionHandler extends ManagerShutdown {
    Flux<Security> requestPositionsFromBrokerage();
}
