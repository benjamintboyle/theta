package theta.api;

import io.reactivex.Flowable;
import theta.execution.api.ExecutableOrder;

public interface ExecutionHandler {
  public Flowable<String> executeStockMarketOrder(ExecutableOrder order);

  public Flowable<String> cancelStockMarketOrder(ExecutableOrder order);
}
