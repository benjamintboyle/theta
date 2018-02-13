package theta.execution.domain;

import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;

public class DefaultOrderStatus implements OrderStatus {

  private ExecutableOrder executableOrder;
  private OrderState orderState;
  private double commission;
  private long filled;
  private long remaining;
  private double averagePrice;

  public DefaultOrderStatus(ExecutableOrder executableOrder, OrderState orderState, double commission, long filled,
      long remaining, double averagePrice) {

    this.executableOrder = executableOrder;
    this.orderState = orderState;
    this.commission = commission;
    this.filled = filled;
    this.remaining = remaining;
    this.averagePrice = averagePrice;
  }

  @Override
  public ExecutableOrder getOrder() {
    return executableOrder;
  }

  @Override
  public OrderState getState() {
    return orderState;
  }

  @Override
  public double getCommission() {
    return commission;
  }

  @Override
  public long getFilled() {
    return filled;
  }

  @Override
  public long getRemaining() {
    return remaining;
  }

  @Override
  public double averagePrice() {
    return averagePrice;
  }

}
