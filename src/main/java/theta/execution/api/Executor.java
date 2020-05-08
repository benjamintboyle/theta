package theta.execution.api;

import java.util.Optional;
import reactor.core.publisher.Mono;
import theta.api.ManagerShutdown;
import theta.domain.Ticker;
import theta.domain.stock.Stock;

public interface Executor extends ManagerShutdown {
  Mono<Void> reverseTrade(Stock trade, ExecutionType executionType, Optional<Double> limitPrice);

  void convertToMarketOrderIfExists(Ticker ticker);
}
