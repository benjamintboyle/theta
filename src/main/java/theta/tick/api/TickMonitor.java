package theta.tick.api;

public interface TickMonitor {

  public void addMonitor(PriceLevel theta);

  public Integer deleteMonitor(PriceLevel theta);
}
