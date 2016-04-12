package theta.portfolio.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.PositionHandler;
import theta.domain.Option;
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
	private Map<UUID, UUID> securityPositionMap = new HashMap<UUID, UUID>();
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

	// Removes positions if security is contained within it
	private void removePositionIfExists(Security security) {
		// If security is mapped to a position, remove position and
		// position-security map entry
		if (this.securityPositionMap.containsKey(security.getId())) {
			ThetaTrade theta = this.positionMap.remove(this.securityPositionMap.remove(security.getId()));
			if (!this.positionMap.values().stream().filter(ticker -> ticker.getTicker().equals(theta.getTicker()))
					.findAny().isPresent()) {
				this.monitor.deleteMonitor(theta);
			}
		}
	}

	private void processSecurity(List<Security> stockList, List<Security> callList, List<Security> putList) {
		logger.info("Processing stock list: {}, call list: {}, put list: {}", stockList, callList, putList);

		Optional<ThetaTrade> optionalTheta = ThetaTrade.of((Stock) stockList.get(0), (Option) callList.get(0),
				(Option) putList.get(0));
		if (optionalTheta.isPresent()) {
			ThetaTrade theta = optionalTheta.get();
			this.positionMap.put(theta.getId(), theta);
			this.securityPositionMap.put(theta.getEquity().getId(), theta.getId());
			this.securityPositionMap.put(theta.getCall().getId(), theta.getId());
			this.securityPositionMap.put(theta.getPut().getId(), theta.getId());
			this.monitor.addMonitor(theta);
		}
	}

	private void processPosition(Security security) {

		// If security changed from previous value associated with Security's Id
		if (!security.equals(this.securityIdMap.put(security.getId(), security))) {
			removePositionIfExists(security);

			// process securities into positions
			// SecurityType -> Ticker -> Price -> Security
			Map<SecurityType, Map<String, Map<Double, List<Security>>>> unassignedSecurities = this.securityIdMap
					.keySet().stream().filter(id -> !this.securityPositionMap.containsKey(id))
					.map(id -> this.securityIdMap.get(id)).collect(Collectors.groupingBy(Security::getSecurityType,
							Collectors.groupingBy(Security::getTicker, Collectors.groupingBy(Security::getPrice))));

			List<String> allUnassignedStockTickers = unassignedSecurities.entrySet().stream()
					.filter(type -> type.getKey().equals(SecurityType.STOCK)).map(type -> type.getValue())
					.flatMap(ticker -> ticker.keySet().stream()).distinct().collect(Collectors.toList());

			for (String ticker : allUnassignedStockTickers) {
				// All unassigned stocks for specific ticker
				List<Security> stockList = unassignedSecurities.get(SecurityType.STOCK).get(ticker).entrySet().stream()
						.map(Entry::getValue).flatMap(List::stream).collect(Collectors.toList());

				List<Double> callPrices = unassignedSecurities.entrySet().stream()
						.filter(type -> type.getKey().equals(SecurityType.CALL))
						.flatMap(type -> type.getValue().entrySet().stream())
						.filter(tickerList -> tickerList.getKey().equals(ticker))
						.map(tickerList -> tickerList.getValue()).flatMap(price -> price.keySet().stream())
						.collect(Collectors.toList());

				for (Double callPrice : callPrices) {
					List<Security> callList = unassignedSecurities.entrySet().stream()
							.filter(type -> type.getKey().equals(SecurityType.CALL))
							.flatMap(type -> type.getValue().entrySet().stream())
							.filter(tickerList -> tickerList.getKey().equals(ticker))
							.flatMap(tickerList -> tickerList.getValue().entrySet().stream())
							.filter(price -> price.getKey().equals(callPrice))
							.flatMap(price -> price.getValue().stream()).collect(Collectors.toList());

					List<Security> putList = unassignedSecurities.entrySet().stream()
							.filter(type -> type.getKey().equals(SecurityType.PUT))
							.flatMap(type -> type.getValue().entrySet().stream())
							.filter(tickerList -> tickerList.getKey().equals(ticker))
							.flatMap(tickerList -> tickerList.getValue().entrySet().stream())
							.filter(price -> price.getKey().equals(callPrice))
							.flatMap(price -> price.getValue().stream()).collect(Collectors.toList());

					if (!putList.isEmpty()) {
						Integer stockQuantityLong = stockList.stream().mapToInt(stock -> stock.getQuantity())
								.filter(quantity -> quantity > 0).sum();
						Integer stockQuantityShort = stockList.stream().mapToInt(stock -> stock.getQuantity())
								.filter(quantity -> quantity < 0).sum();

						logger.info("{} Long Stock Quantity: {}, {} Short Stock Quantity: {}", ticker,
								stockQuantityLong, ticker, stockQuantityShort);

						// if there is enough stock
						if (stockQuantityLong % 100 == 0 && stockQuantityLong > 0) {
							processSecurity(stockList.stream().filter(stock -> stock.getQuantity() > 0)
									.collect(Collectors.toList()), callList, putList);
						}
						if (stockQuantityShort % 100 == 0 && stockQuantityShort < 0) {
							processSecurity(stockList.stream().filter(stock -> stock.getQuantity() < 0)
									.collect(Collectors.toList()), callList, putList);
						}
					}
				}
			}
		} else {
			logger.warn("Security did not change from previous update: {}", security);
		}

		this.logPositions();
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
		return this.positionMap.values().stream().filter(position -> position.getTicker().equals(ticker))
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

		for (Security security : this.securityIdMap.values().stream()
				.filter(security -> !this.securityPositionMap.containsKey(security.getId()))
				.collect(Collectors.toList())) {
			logger.info("Current unprocessed security: {}", security);
		}
	}
}
