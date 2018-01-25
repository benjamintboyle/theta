package theta.execution.api;

import io.reactivex.Completable;
import theta.domain.Stock;

public interface Executor {
  public Completable reverseTrade(Stock trade);
}
