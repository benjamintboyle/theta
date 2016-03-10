package theta.portfolio.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.api.Security;
import theta.domain.ThetaTrade;
import theta.execution.api.ExecutionMonitor;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;

public class PortfolioManager implements PortfolioObserver, PositionProvider {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private PositionHandler positionHandler;
	private Monitor monitor;
	private ExecutionMonitor executionMonitor;
	private List<ThetaTrade> positions = new ArrayList<ThetaTrade>();
	UnprocessedPositionManager unprocessedPositionManager = new UnprocessedPositionManager();

	public PortfolioManager(PositionHandler positionHandler) {
		logger.info("Starting Portfolio Manager");
		this.positionHandler = positionHandler;
		this.positionHandler.subscribePositions(this);
	}

	public PortfolioManager(PositionHandler positionHandler, Monitor monitor) {
		this(positionHandler);
		this.monitor = monitor;
	}

	public void processPosition(ThetaTrade theta) {
		List<ThetaTrade> matchingThetas = this.positions.stream()
				.filter(position -> theta.getTicker().equals(position.getTicker()))
				.filter(position -> theta.getStrikePrice().equals(position.getStrikePrice()))
				.collect(Collectors.toList());

		switch (matchingThetas.size()) {
		case 0:
			this.positions.add(theta);
			logger.info("Added new position: {}", theta);
			break;
		case 1:
			matchingThetas.get(0).add(theta);
			logger.info("Added to existing theta: {}", theta);
			break;
		default:
			logger.error("There should never be multiple trades at the same strike: {}", matchingThetas);
		}

	}

	@Override
	public void ingestPosition(Security security) {
		logger.info("Received Position update: {}", security.toString());

		this.executionMonitor.portfolioChange(security);

		Optional<ThetaTrade> optionalTheta = this.unprocessedPositionManager.add(security);

		if (optionalTheta.isPresent()) {
			ThetaTrade theta = optionalTheta.get();

			logger.info("Adding Theta to positions: {}", theta);

			this.monitor.addMonitor(theta);
			this.processPosition(theta);
		}

		/*
		 * for (ThetaTrade position : this.positions) { if
		 * (position.getBackingTicker().equals(security.getBackingTicker())) {
		 * switch (security.getSecurityType()) { case STOCK: if
		 * (!position.hasEquity()) { position.add(security); if
		 * (position.isComplete()) { this.monitor.addMonitor(position); }
		 * return; } else { if (position.getEquity().getQuantity() == -1 *
		 * security.getQuantity()) { position.reversePosition(); } } break; case
		 * CALL: case PUT: if (!position.hasOption(security)) { Option option =
		 * (Option) security;
		 * 
		 * if (position.getStrikePrice().equals(option.getStrikePrice())) {
		 * position.add(option); if (position.isComplete()) {
		 * this.monitor.addMonitor(position); } return; } else if
		 * (!position.hasOption()) { if (Math.round(option.getStrikePrice()) ==
		 * Math.round(position.getStrikePrice())) { position.add(option); if
		 * (position.isComplete()) { this.monitor.addMonitor(position); }
		 * return; } } } break; default: this.logger.error(
		 * "Unknown Security Type: {}", security.toString()); }
		 * 
		 * } }
		 * 
		 * this.positions.add(new ThetaTrade(security));
		 */
	}

	@Override
	public List<ThetaTrade> providePositions(String ticker) {
		logger.info("Providing Positions for: {}", ticker);
		return this.positions.parallelStream().filter(position -> position.getTicker().equals(ticker))
				.filter(position -> position.isComplete()).collect(Collectors.toList());
	}

	public void registerTickMonitor(Monitor monitor) {
		logger.info("Registering Tick Monitor with Portfolio Monitor");
		this.monitor = monitor;
	}

	public void registerExecutionMonitor(ExecutionMonitor executionMonitor) {
		logger.info("Registering Execution Monitor with Portfolio Manager");
		this.executionMonitor = executionMonitor;
	}

	public void logPositions() {
		for (ThetaTrade position : this.positions) {
			logger.info("Current position: {}", position);
		}
	}
}
