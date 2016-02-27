package theta.portfolio.manager;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brokers.interactive_brokers.handlers.IbPositionHandler;
import theta.api.Security;
import theta.connection.api.Controller;
import theta.domain.Option;
import theta.domain.ThetaTrade;
import theta.portfolio.api.PortfolioReceiver;
import theta.tick.api.Monitor;

public class PortfolioManager implements PortfolioReceiver {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private Controller controllor;
	private Monitor monitor;
	private ArrayList<ThetaTrade> positions = new ArrayList<ThetaTrade>();

	// Handlers
	private IbPositionHandler ibPositionHander = new IbPositionHandler(this);

	public PortfolioManager(Controller controllor, Monitor monitor) {
		this.logger.info("Starting subsystem: 'Portfolio Manager'");

		this.controllor = controllor;
		this.monitor = monitor;

		this.controllor.getController().reqPositions(this.ibPositionHander);
	}

	@Override
	public void ingestPosition(Security security) {
		this.logger.info("Received Position update: {}", security.toString());
		for (ThetaTrade position : this.positions) {
			if (position.getBackingTicker().equals(security.getBackingTicker())) {
				switch (security.getSecurityType()) {
				case STOCK:
					if (!position.hasEquity()) {
						position.add(security);
						if (position.isComplete()) {
							this.monitor.addMonitor(position);
						}
						return;
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
