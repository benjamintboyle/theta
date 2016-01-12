package theta;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController;

import brokers.interactive_brokers.IbPositionHandler;
import brokers.interactive_brokers.IbTickHandler;
import theta.managers.ConnectionManager;
import theta.managers.ExecutionManager;
import theta.managers.PortfolioManager;
import theta.managers.PriceMonitor;
import theta.managers.api.MarketDataRequester;
import theta.managers.api.PortfolioRequester;
import theta.strategies.ThetaTrade;

public class ThetaEngine implements PortfolioRequester, MarketDataRequester {
	public final static Logger logger = LoggerFactory.getLogger(ThetaEngine.class);
	private final static String SYSTEM_NAME = "ThetaEngine";

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
		ThetaEngine.logger.info("Starting system: '{}'", ThetaEngine.SYSTEM_NAME);
		new ThetaEngine();
	}

	public ApiController controller() {
		return this.connectionManager.controller();
	}

	public ArrayList<String> getAccountList() {
		return this.connectionManager.getAccountList();
	}

	public void addMonitor(ThetaTrade trade) {
		this.monitor.addMonitor(trade);
	}

	public void shutdown() {
		// Signal main loop to end
		logger.info("Shutting down '{}' system", ThetaEngine.SYSTEM_NAME);
	}

	public void reverseTrade(ThetaTrade trade) {
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
	public void subscribeMarketData(ThetaTrade trade) {
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
