package theta.domain;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

import brokers.interactive_brokers.IbOrderHandler;
import brokers.interactive_brokers.IbPositionHandler;
import brokers.interactive_brokers.IbTickHandler;
import theta.connection.manager.ConnectionManager;
import theta.execution.api.Executable;
import theta.execution.manager.ExecutionManager;
import theta.managers.api.MarketDataRequester;
import theta.portfolio.api.PortfolioRequester;
import theta.portfolio.manager.PortfolioManager;
import theta.properties.manager.PropertiesManager;
import theta.tick.manager.TickManager;

public class ThetaEngine implements PortfolioRequester, MarketDataRequester {
	public final static Logger logger = LoggerFactory.getLogger(ThetaEngine.class);
	private final static String SYSTEM_NAME = "ThetaEngine";

	// Managers
	private final PropertiesManager propertiesManager = new PropertiesManager("config.properties");
	private final ConnectionManager connectionManager = new ConnectionManager(
			this.propertiesManager.getProperty("GATEWAY_HOST"),
			Integer.parseInt(this.propertiesManager.getProperty("GATEWAY_PORT")),
			Integer.parseInt(this.propertiesManager.getProperty("CLIENT_ID")));
	private final TickManager monitor = new TickManager(this);
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

	public void execute(Executable order) {
		NewOrder ibOrder = new NewOrder();

		if (order.getQuantity() > 0) {
			ibOrder.action(Action.SELL);
		} else {
			ibOrder.action(Action.BUY);
		}
		ibOrder.totalQuantity(2 * Math.abs(order.getQuantity()));
		ibOrder.orderType(OrderType.MKT);
		ibOrder.orderId(0);

		NewContract contract = new NewContract(new Contract());

		this.controller().placeOrModifyOrder(contract, ibOrder, new IbOrderHandler());
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
