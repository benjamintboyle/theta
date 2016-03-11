package theta.portfolio.api;

import theta.domain.api.Security;

public interface PortfolioObserver {

	public void ingestPosition(Security security);
}
