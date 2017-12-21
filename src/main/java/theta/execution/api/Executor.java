package theta.execution.api;

import theta.domain.Stock;

public interface Executor {
  public void reverseTrade(Stock trade);
}
