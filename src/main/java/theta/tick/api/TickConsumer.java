package theta.tick.api;

public interface TickConsumer {
  public void acceptTick(String ticker);
}
