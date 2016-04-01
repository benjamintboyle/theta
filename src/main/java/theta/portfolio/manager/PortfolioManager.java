package theta.portfolio.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutionMonitor;
import theta.portfolio.api.PortfolioObserver;
import theta.portfolio.api.PositionProvider;
import theta.tick.api.Monitor;

public class PortfolioManager implements PortfolioObserver, PositionProvider, Runnable {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private PositionHandler positionHandler;
	private Monitor monitor;
	private ExecutionMonitor executionMonitor;
	private Map<UUID, ThetaTrade> positionMap = new HashMap<UUID, ThetaTrade>();
	private Map<UUID, Security> securityIdMap = new HashMap<UUID, Security>();
	private Map<UUID, UUID> positionSecurityMap = new HashMap<UUID, UUID>();
	// UnprocessedPositionManager unprocessedPositionManager = new
	// UnprocessedPositionManager(this);
	private BlockingQueue<Security> positionQueue = new LinkedBlockingQueue<Security>();

	private Boolean running = true;

	public PortfolioManager(PositionHandler positionHandler) {
		logger.info("Starting Portfolio Manager");
		this.positionHandler = positionHandler;
		this.positionHandler.subscribePositions(this);
	}

	public PortfolioManager(PositionHandler positionHandler, Monitor monitor) {
		this(positionHandler);
		this.monitor = monitor;
	}

	@Override
	public void ingestPosition(Security security) {
		logger.info("Received Position update: {}", security.toString());
		try {
			this.positionQueue.put(security);
		} catch (InterruptedException e) {
			logger.error("Interupted before security could be added", e);
		}
	}

	private void processPosition(Security security) {
		// Update security list
		// process security list

		// If security changed from previous value associated with Security's Id
		if (!security.equals(this.securityIdMap.put(security.getId(), security))) {
			// If security is mapped to a position, remove position and
			// position-security map entry
			if (this.positionSecurityMap.containsKey(security.getId())) {
				this.positionMap.remove(this.positionSecurityMap.remove(security.getId()));
			}

			// process securities into positions
			Map<SecurityType, Map<String, Map<Double, List<Security>>>> unassignedSecurities = this.securityIdMap
					.keySet().parallelStream().filter(id -> !this.positionSecurityMap.containsKey(id))
					.map(id -> this.securityIdMap.get(id)).collect(Collectors.groupingBy(Security::getSecurityType,
							Collectors.groupingBy(Security::getTicker, Collectors.groupingBy(Security::getPrice))));
			for (String ticker : unassignedSecurities.get(SecurityType.CALL).keySet()) {

				List<Security> stockList = unassignedSecurities.get(SecurityType.STOCK).get(ticker).entrySet().stream().map(price -> price.getValue()).m

				for (Double callPrice : unassignedSecurities.get(SecurityType.CALL).get(ticker).keySet()) {
					List<Security> callList = unassignedSecurities.get(SecurityType.CALL).get(ticker).get(callPrice);
					List<Security> putList = unassignedSecurities.get(SecurityType.PUT).get(ticker).get(callPrice);
				
					
				}
			}
		}

		this.executionMonitor.portfolioChange(security);
	}

	@Override
	public void run() {
		while (this.running) {
			try {
				Security security = this.positionQueue.take();

				this.processPosition(security);
			} catch (InterruptedException e) {
				logger.error("Interupted while waiting for security", e);
			}
		}
	}

	@Override
	public List<ThetaTrade> providePositions(String ticker) {
		logger.info("Providing Positions for: {}", ticker);
		return this.positionMap.values().parallelStream().filter(position -> position.getTicker().equals(ticker))
				.filter(position -> position.isComplete()).collect(Collectors.toList());
	}

	public void registerTickMonitor(Monitor monitor) {
		logger.info("Registering Tick Monitor with Portfolio Manager");
		this.monitor = monitor;
	}

	public void registerExecutionMonitor(ExecutionMonitor executionMonitor) {
		logger.info("Registering Execution Monitor with Portfolio Manager");
		this.executionMonitor = executionMonitor;
	}

	public void logPositions() {
		for (ThetaTrade position : this.positionMap.values()) {
			logger.info("Current position: {}", position);
		}
	}
}
