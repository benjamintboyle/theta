package theta.managers.api;

import theta.strategies.api.Security;

public interface PortfolioReceiver {

	public void ingestPosition(Security security);
}
