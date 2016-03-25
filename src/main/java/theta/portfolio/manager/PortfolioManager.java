package theta.portfolio.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutionMonitor;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;

public class PortfolioManager implements PortfolioObserver, PositionProvider {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private PositionHandler positionHandler;
	private Monitor monitor;
	private ExecutionMonitor executionMonitor;
	private List<ThetaTrade> positions = new ArrayList<ThetaTrade>();
	UnprocessedPositionManager unprocessedPositionManager = new UnprocessedPositionManager(this);

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
			this.monitor.addMonitor(theta);
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

	private List<ThetaTrade> getCurrentPositionsThatMatch(Security security) {
		List<ThetaTrade> tickMatchingThetas = this.positions.stream()
				.filter(theta -> theta.getTicker().equals(security.getTicker())).collect(Collectors.toList());

		if (SecurityType.CALL.equals(security.getSecurityType())
				|| SecurityType.PUT.equals(security.getSecurityType())) {
			tickMatchingThetas = tickMatchingThetas.stream()
					.filter(theta -> theta.getStrikePrice().equals(security.getPrice())).collect(Collectors.toList());
		}

		return tickMatchingThetas;
	}

	private List<Security> getDeltaOfMatch(Security security, List<ThetaTrade> matchingPositions) {
		return matchingPositions.stream().flatMap(theta -> theta.toSecurityList().stream())
				.filter(matchingSecurity -> !matchingSecurity.getSecurityType().equals(security.getSecurityType()))
				.collect(Collectors.toList());
	}

	@Override
	public void ingestPosition(Security security) {
		logger.info("Received Position update: {}", security.toString());

		// check for thetas
		List<ThetaTrade> matchingPositions = this.getCurrentPositionsThatMatch(security);

		// remove positions
		this.positions.removeAll(matchingPositions);
		this.monitor.deleteMonitor(security.getTicker());

		// get securities to re-process
		List<Security> unprocessedSecurities = new ArrayList<Security>();
		unprocessedSecurities.add(security);
		unprocessedSecurities.addAll(this.getDeltaOfMatch(security, matchingPositions));

		// send to unprocessed
		this.unprocessedPositionManager.processSecurities(unprocessedSecurities);

		this.executionMonitor.portfolioChange(security);
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
