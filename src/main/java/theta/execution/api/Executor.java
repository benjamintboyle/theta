package theta.execution.api;

import java.util.Optional;
import io.reactivex.Completable;
import theta.domain.Stock;

public interface Executor {
  public Completable reverseTrade(Stock trade, ExecutionType executionType, Optional<Double> limitPrice);
}
