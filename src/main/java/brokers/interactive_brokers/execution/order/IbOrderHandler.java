package brokers.interactive_brokers.execution.order;

import com.ib.controller.ApiController.IOrderHandler;
import reactor.core.publisher.Flux;
import theta.execution.api.OrderStatus;

public interface IbOrderHandler extends IOrderHandler {
    Flux<OrderStatus> getOrderStatus();
}
