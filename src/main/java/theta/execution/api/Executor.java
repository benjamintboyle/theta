package theta.execution.api;

import java.util.Optional;
import io.reactivex.Completable;
import theta.domain.Stock;
import theta.domain.Ticker;

public interface Executor {
  public Completable reverseTrade(Stock trade, ExecutionType executionType, Optional<Double> limitPrice);

  public void convertToMarketOrderIfExists(Ticker ticker);
}
