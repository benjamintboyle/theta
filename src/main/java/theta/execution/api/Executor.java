package theta.execution.api;

import io.reactivex.rxjava3.core.Completable;
import java.util.Optional;
import theta.api.ManagerShutdown;
import theta.domain.Ticker;
import theta.domain.stock.Stock;

public interface Executor extends ManagerShutdown {
  Completable reverseTrade(Stock trade, ExecutionType executionType, Optional<Double> limitPrice);

  void convertToMarketOrderIfExists(Ticker ticker);
}
