package theta.tick.api;

public interface TickObserver {
  public void acceptTick(String ticker);
}
