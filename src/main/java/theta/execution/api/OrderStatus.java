package theta.execution.api;

public interface OrderStatus {

  public ExecutableOrder getOrder();

  public OrderState getState();

  public double getCommission();

  public long getFilled();

  public long getRemaining();

  public double averagePrice();

}
