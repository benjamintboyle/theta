package theta.execution.domain;

import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;

public class DefaultOrderStatus implements OrderStatus {

  private final ExecutableOrder executableOrder;
  private final OrderState orderState;
  private final double commission;
  private final long filled;
  private final long remaining;
  private final double averagePrice;

  /**
   * Create Default Order Status.
   *
   * @param executableOrder Actual order
   * @param orderState Order State
   * @param commission Commission amount
   * @param filled Amount of order filled
   * @param remaining Amount of order remaining to be filled
   * @param averagePrice Average fill price
   */
  public DefaultOrderStatus(ExecutableOrder executableOrder, OrderState orderState,
      double commission, long filled, long remaining, double averagePrice) {

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
  public double getAveragePrice() {
    return averagePrice;
  }

  @Override
  public String toString() {

    final StringBuilder builder = new StringBuilder();

    builder.append("Order Status [ ");

    builder.append(getOrder());

    builder.append(", Order State: ");
    builder.append(getState());

    builder.append(", Commission: ");
    builder.append(getCommission());

    builder.append(", Filled: ");
    builder.append(getFilled());

    builder.append(", Remaining: ");
    builder.append(getRemaining());

    builder.append(", Average Price: ");
    builder.append(getAveragePrice());

    builder.append(" ]");

    return builder.toString();
  }

}
