package theta.execution.api;

import java.util.Optional;
import io.reactivex.Completable;
import theta.domain.Ticker;
import theta.domain.stock.Stock;

public interface Executor {
  public Completable reverseTrade(Stock trade, ExecutionType executionType, Optional<Double> limitPrice);

  public void convertToMarketOrderIfExists(Ticker ticker);
}
