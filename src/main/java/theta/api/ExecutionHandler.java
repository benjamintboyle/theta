package theta.api;

import io.reactivex.Flowable;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

public interface ExecutionHandler {
  public Flowable<OrderStatus> executeOrder(ExecutableOrder order);

  public boolean modifyOrder(ExecutableOrder order);

  public Flowable<OrderStatus> cancelOrder(ExecutableOrder order);
}
