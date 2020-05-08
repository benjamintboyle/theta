package theta.api;

import reactor.core.publisher.Flux;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderStatus;

public interface ExecutionHandler {
  Flux<OrderStatus> executeOrder(ExecutableOrder order);

  boolean modifyOrder(ExecutableOrder order);

  Flux<OrderStatus> cancelOrder(ExecutableOrder order);
}
