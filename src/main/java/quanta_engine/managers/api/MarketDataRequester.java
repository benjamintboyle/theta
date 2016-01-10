package quanta_engine.managers.api;

import quanta_engine.strategies.ExtrinsicCapture;

public interface MarketDataRequester {

	public void subscribeMarketData(ExtrinsicCapture trade);

	public void unsubscribeMarketData(String ticker);

	public Double getLast(String ticker);
}
