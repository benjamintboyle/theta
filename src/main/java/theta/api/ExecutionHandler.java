package theta.api;

import io.reactivex.rxjava3.core.Flowable;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

public interface ExecutionHandler {
  Flowable<OrderStatus> executeOrder(ExecutableOrder order);

  boolean modifyOrder(ExecutableOrder order);

  Flowable<OrderStatus> cancelOrder(ExecutableOrder order);
}
