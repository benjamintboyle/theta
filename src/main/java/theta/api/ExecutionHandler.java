package theta.api;

import io.reactivex.Flowable;
import theta.execution.api.ExecutableOrder;

public interface ExecutionHandler {
  public Flowable<String> executeStockOrder(ExecutableOrder order);

  public Flowable<String> cancelStockOrder(ExecutableOrder order);
}
