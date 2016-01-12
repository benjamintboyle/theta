package theta.managers.api;

import theta.strategies.ExtrinsicCapture;

public interface MarketDataRequester {

	public void subscribeMarketData(ExtrinsicCapture trade);

	public void unsubscribeMarketData(String ticker);

	public Double getLast(String ticker);
}
