package theta.portfolio.api;

import theta.api.Security;

public interface PortfolioObserver {

	public void ingestPosition(Security security);
}
