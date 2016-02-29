package theta.portfolio.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.api.Security;
import theta.domain.Option;
import theta.domain.ThetaTrade;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;

public class PortfolioManager implements PortfolioObserver, PositionProvider {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private PositionHandler positionHandler;
	private Monitor monitor;
	private ArrayList<ThetaTrade> positions = new ArrayList<ThetaTrade>();

	public PortfolioManager(PositionHandler positionHandler) {
		this.logger.info("Starting subsystem: 'Portfolio Manager'");
		this.positionHandler = positionHandler;
		this.positionHandler.subscribePositions(this);
	}

	public PortfolioManager(PositionHandler positionHandler, Monitor monitor) {
		this(positionHandler);
		this.monitor = monitor;
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

	@Override
	public List<ThetaTrade> providePositions(String ticker) {
		return this.positions.parallelStream().filter(position -> position.getBackingTicker().equals(ticker))
				.filter(position -> position.isComplete()).collect(Collectors.toList());
	}

	public void registerMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
}
