package theta.api;

import io.reactivex.Flowable;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

public interface ExecutionHandler {
  public Flowable<OrderStatus> executeStockOrder(ExecutableOrder order);

  public boolean modifyStockOrder(ExecutableOrder order);

  public Flowable<OrderStatus> cancelStockOrder(ExecutableOrder order);
}
