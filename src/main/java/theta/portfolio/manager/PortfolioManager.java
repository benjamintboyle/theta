package theta.portfolio.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

		// If security changed from previous value associated with Security's Id
		if (!security.equals(this.securityIdMap.put(security.getId(), security))) {

			// If security is mapped to a position, remove position and
			// position-security map entry
			if (this.positionSecurityMap.containsKey(security.getId())) {
				ThetaTrade theta = this.positionMap.remove(this.positionSecurityMap.remove(security.getId()));
				if (!this.positionMap.values().parallelStream()
						.filter(ticker -> ticker.getTicker().equals(theta.getTicker())).findAny().isPresent()) {
					this.monitor.deleteMonitor(theta);
				}
			}

			// process securities into positions
			Map<SecurityType, Map<String, Map<Double, List<Security>>>> unassignedSecurities = this.securityIdMap
					.keySet().parallelStream().filter(id -> !this.positionSecurityMap.containsKey(id))
					.map(id -> this.securityIdMap.get(id)).collect(Collectors.groupingBy(Security::getSecurityType,
							Collectors.groupingBy(Security::getTicker, Collectors.groupingBy(Security::getPrice))));
			for (String ticker : unassignedSecurities.get(SecurityType.CALL).keySet()) {

				// All stocks unassigned stocks for specific ticker
				List<Security> stockList = unassignedSecurities.get(SecurityType.STOCK).get(ticker).entrySet().stream()
						.map(Entry::getValue).flatMap(List::stream).collect(Collectors.toList());

				// if there is enough stock
				if (stockList.stream().mapToInt(stock -> stock.getQuantity()).sum() >= 100) {
					// For each price level of call options
					for (Double callPrice : unassignedSecurities.get(SecurityType.CALL).get(ticker).keySet()) {
						List<Security> callListAtPrice = unassignedSecurities.get(SecurityType.CALL).get(ticker)
								.get(callPrice);
						List<Security> putListAtPrice = unassignedSecurities.get(SecurityType.PUT).get(ticker)
								.get(callPrice);
						// at this point we know stock and call are valid, but
						// not put
						if (putListAtPrice.size() > 0) {
							Stock stock = new Stock(stockList.get(0).getId(), stockList.get(0).getTicker(),
									stockList.get(0).getQuantity(), stockList.get(0).getPrice());
							Option call = new Option(callListAtPrice.get(0).getId(), SecurityType.CALL, callListAtPrice.get(0).getTicker(), callListAtPrice.get(0).getQuantity(), callListAtPrice.get(0).getPrice(), callListAtPrice.get(0))
							ThetaTrade theta = ThetaTrade.of(stockList.get(0), callListAtPrice.get(0),
									putListAtPrice.get(0));
						}
					}
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
