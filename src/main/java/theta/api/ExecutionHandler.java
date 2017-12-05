package theta.api;

import io.reactivex.Flowable;
import theta.execution.api.ExecutableOrder;

public interface ExecutionHandler {
  public Flowable<String> executeStockEquityMarketOrder(ExecutableOrder order);
}
