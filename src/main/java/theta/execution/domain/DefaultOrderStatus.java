package theta.execution.domain;

import theta.execution.api.ExecutableOrder;
import theta.execution.api.OrderState;
import theta.execution.api.OrderStatus;

import java.util.Objects;

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
     * @param orderState      Order State
     * @param commission      Commission amount
     * @param filled          Amount of order filled
     * @param remaining       Amount of order remaining to be filled
     * @param averagePrice    Average fill price
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
        return "DefaultOrderStatus{" +
                "executableOrder=" + executableOrder +
                ", orderState=" + orderState +
                ", commission=" + commission +
                ", filled=" + filled +
                ", remaining=" + remaining +
                ", averagePrice=" + averagePrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultOrderStatus that = (DefaultOrderStatus) o;
        return Double.compare(that.commission, commission) == 0 &&
                filled == that.filled &&
                remaining == that.remaining &&
                Double.compare(that.averagePrice, averagePrice) == 0 &&
                executableOrder.equals(that.executableOrder) &&
                orderState == that.orderState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(executableOrder, orderState, commission, filled, remaining, averagePrice);
    }
}
