package quanta_engine.managers.api;

import quanta_engine.strategies.api.Security;

public interface PortfolioReceiver {

	public void ingestPosition(Security security);
}
