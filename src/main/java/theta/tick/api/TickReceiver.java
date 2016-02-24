package theta.tick.api;

public interface TickReceiver {
	public Boolean notifyPriceChange(String ticker);
}
