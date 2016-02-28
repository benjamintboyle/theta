package theta.portfolio.api;

import theta.api.Security;

public interface PortfolioReceiver {

	public void ingestPosition(Security security);
}
