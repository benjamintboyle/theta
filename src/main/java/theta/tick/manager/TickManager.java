package theta.tick.manager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brokers.interactive_brokers.handlers.IbTickHandler;
import theta.connection.api.Controllor;
import theta.domain.ThetaTrade;
import theta.execution.api.Executor;
import theta.tick.api.Monitor;
import theta.tick.api.TickReceiver;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;

public class TickManager implements Monitor, TickReceiver {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	Controllor controllor;
	Executor executor;

	private Map<String, ThetaTrade> monitoredTrades = new HashMap<String, ThetaTrade>();
	private HashMap<String, IbTickHandler> tickHandlers = new HashMap<String, IbTickHandler>();

	public TickManager(Controllor controllor, Executor executor) {
		this.logger.info("Starting subsystem: 'Monitor'");
		this.controllor = controllor;
		this.executor = executor;
	}

	@Override
	public void addMonitor(ThetaTrade trade) {
		this.logger.info("Adding Monitor for '{}'", trade.getBackingTicker());

		this.monitoredTrades.put(trade.getBackingTicker(), trade);

		if (!this.tickHandlers.containsKey(trade.getBackingTicker())) {
			this.tickHandlers.put(trade.getBackingTicker(), new IbTickHandler(this.controllor, this, trade));
		}
	}

	public ThetaTrade deleteMonitor(String ticker) {
		return this.monitoredTrades.remove(ticker);
	}

	@Override
	public Boolean notifyTick(Tick tick) {
		Boolean tradeReversed = Boolean.FALSE;

		// Only check if it is a "LAST" tick (rather than ASK, BID, etc)
		if (!TickType.LAST.equals(tick.getTickType())) {
			return tradeReversed;
		}

		ThetaTrade trade = this.monitoredTrades.get(tick.getTicker());
		Double lastPrice = tick.getPrice();
		Integer quantity = trade.getEquity().getQuantity();
		Double strikePrice = trade.getStrikePrice();

		this.logger.info("Current tick for '{}': ${}", trade.getBackingTicker(), lastPrice);

		if ((quantity > 0) && (lastPrice < strikePrice)) {
			tradeReversed = this.reversePosition(trade);
		} else if ((quantity < 0) && (lastPrice > strikePrice)) {
			tradeReversed = this.reversePosition(trade);
		}

		return tradeReversed;
	}

	private Boolean reversePosition(ThetaTrade trade) {
		this.logger.info("Reversing position in '{}'}", trade.getBackingTicker());
		this.executor.reverseTrade(trade);
		this.monitoredTrades.put(trade.getBackingTicker(), trade.reversePosition());
		return Boolean.TRUE;
	}
}
