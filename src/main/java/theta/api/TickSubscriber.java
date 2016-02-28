package theta.api;

public interface TickSubscriber {
	public TickHandler subscribeEquity(String ticker);

	public Boolean unsubscribeEquity(TickHandler tickHandler);
}
