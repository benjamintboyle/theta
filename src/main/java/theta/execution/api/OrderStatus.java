package theta.execution.api;

public interface OrderStatus {
    ExecutableOrder getOrder();

    OrderState getState();

    double getCommission();

    long getFilled();

    long getRemaining();

    double getAveragePrice();
}
