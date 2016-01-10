package quanta_engine;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;

import brokers.interactive_brokers.IbPositionHandler;
import brokers.interactive_brokers.IbTickHandler;
import quanta_engine.managers.ConnectionManager;
import quanta_engine.managers.ExecutionManager;
import quanta_engine.managers.PortfolioManager;
import quanta_engine.managers.PriceMonitor;
import quanta_engine.managers.api.MarketDataRequester;
import quanta_engine.managers.api.PortfolioRequester;
import quanta_engine.strategies.ExtrinsicCapture;

public class QuantaEngine implements PortfolioRequester, MarketDataRequester {
	public final static Logger logger = LoggerFactory.getLogger(QuantaEngine.class);
	private final static String SYSTEM_NAME = "QuantaEngine";

	// Managers
	private final ConnectionManager connectionManager = new ConnectionManager();
	private final PriceMonitor monitor = new PriceMonitor(this);
	private final PortfolioManager portfolioManager = new PortfolioManager(this, monitor);
	private final ExecutionManager executionManager = new ExecutionManager(this);

	// Handlers
	private IbPositionHandler ibPositionHander = new IbPositionHandler(this.portfolioManager);
	private HashMap<String, IbTickHandler> tickHandlers = new HashMap<String, IbTickHandler>();

	// Entry point for application
	public static void main(String[] args) {
		QuantaEngine.logger.info("Starting system: '{}'", QuantaEngine.SYSTEM_NAME);
		new QuantaEngine();
	}

	public ApiController controller() {
		return this.connectionManager.controller();
	}

	public ArrayList<String> getAccountList() {
		return this.connectionManager.getAccountList();
	}

	public void addMonitor(ExtrinsicCapture trade) {
		this.monitor.addMonitor(trade);
	}

	public void shutdown() {
		// Signal main loop to end
		logger.info("Shutting down '{}' system", QuantaEngine.SYSTEM_NAME);
	}

	public void reverseTrade(ExtrinsicCapture trade) {
		this.executionManager.reverseTrade(trade);
	}

	@Override
	public void subscribePortfolio() {
		this.controller().reqPositions(this.ibPositionHander);
	}

	@Override
	public void unsubscribePortfolio() {
		this.controller().cancelPositions(this.ibPositionHander);
	}

	@Override
	public void subscribeMarketData(ExtrinsicCapture trade) {
		if (!this.tickHandlers.containsKey(trade.getBackingTicker())) {
			this.tickHandlers.put(trade.getBackingTicker(), new IbTickHandler(this, this.monitor, trade));
		}
	}

	@Override
	public void unsubscribeMarketData(String ticker) {
		this.tickHandlers.get(ticker).unsubscribeMarketData();
		this.tickHandlers.remove(ticker);
	}

	@Override
	public Double getLast(String ticker) {
		return this.tickHandlers.get(ticker).getLast();
	}
}
