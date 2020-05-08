package theta.api;

import reactor.core.publisher.Flux;
import theta.domain.Security;

import java.time.Instant;

public interface PositionHandler extends ManagerShutdown {
  Flux<Security> requestPositionsFromBrokerage();
  Flux<Instant> getPositionEnd();
}
