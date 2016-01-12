package theta.managers.api;

import theta.strategies.ThetaTrade;

public interface MarketDataRequester {

	public void subscribeMarketData(ThetaTrade trade);

	public void unsubscribeMarketData(String ticker);

	public Double getLast(String ticker);
}
