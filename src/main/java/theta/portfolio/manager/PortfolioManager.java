package theta.portfolio.manager;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.Security;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.managers.api.Monitor;
import theta.portfolio.api.PortfolioReceiver;
import theta.portfolio.api.PortfolioRequester;

public class PortfolioManager implements PortfolioReceiver {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private PortfolioRequester request;
	private Monitor monitor;
	private Set<ThetaTrade> positions = new HashSet<ThetaTrade>();

	public PortfolioManager(PortfolioRequester request, Monitor monitor) {
		this.logger.info("Starting subsystem: 'Portfolio Manager'");

		this.request = request;
		this.monitor = monitor;

		this.request.subscribePortfolio();
	}

	@Override
	public void ingestPosition(Security security) {
		this.logger.info("Received Position update: {}", security.toString());
		for (ThetaTrade position : this.positions) {
			if (position.getBackingTicker().equals(security.getBackingTicker())) {
				switch (security.getSecurityType()) {
				case STOCK:
					if (!position.hasEquity()) {
						Stock stock = (Stock) security;
						if (Math.round(stock.getAverageTradePrice()) == position.getStrikePrice()) {
							position.add(security);
							if (position.isComplete()) {
								this.monitor.addMonitor(position);
							}
							return;
						}
					}
					break;
				case CALL:
				case PUT:
					if (!position.hasOption(security)) {
						Option option = (Option) security;

						if (position.getStrikePrice().equals(option.getStrikePrice())) {
							position.add(option);
							if (position.isComplete()) {
								this.monitor.addMonitor(position);
							}
							return;
						} else if (!position.hasOption()) {
							if (Math.round(option.getStrikePrice()) == Math.round(position.getStrikePrice())) {
								position.add(option);
								if (position.isComplete()) {
									this.monitor.addMonitor(position);
								}
								return;
							}
						}
					}
					break;
				default:
					this.logger.error("Unknown Security Type: {}", security.toString());
				}

			}
		}

		this.positions.add(new ThetaTrade(security));
	}
}
