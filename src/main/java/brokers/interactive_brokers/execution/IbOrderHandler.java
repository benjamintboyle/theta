package brokers.interactive_brokers.execution;

import com.ib.controller.ApiController.IOrderHandler;
import theta.execution.api.ExecutableOrder;

public interface IbOrderHandler extends IOrderHandler {

  public ExecutableOrder getExecutableOrder();

}
