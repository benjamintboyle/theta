package theta.managers.api;

import theta.domain.ThetaTrade;

public interface MarketDataRequester {

	public void subscribeMarketData(ThetaTrade trade);

	public void unsubscribeMarketData(String ticker);
}
