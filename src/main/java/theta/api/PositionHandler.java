package theta.api;

import theta.portfolio.api.PortfolioObserver;

public interface PositionHandler {
	public void subscribePositions(PortfolioObserver observer);

	public void requestPositionsFromBrokerage();
}
