package theta.tick.manager;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.domain.ThetaEngine;
import theta.domain.ThetaTrade;
import theta.managers.api.Monitor;

public class TickManager implements Monitor {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	ThetaEngine callback;

	private Map<String, ThetaTrade> monitoredTrades = new HashMap<String, ThetaTrade>();

	public TickManager(ThetaEngine callback) {
		this.logger.info("Starting subsystem: 'Monitor'");
		this.callback = callback;
	}

	@Override
	public void addMonitor(ThetaTrade trade) {
		this.logger.info("Adding Monitor for '{}'", trade.getBackingTicker());

		this.monitoredTrades.put(trade.getBackingTicker(), trade);
		this.callback.subscribeMarketData(trade);

	}

	public ThetaTrade deleteMonitor(String ticker) {
		return this.monitoredTrades.remove(ticker);
	}

	// Main monitor method
	public Boolean notifyPriceChange(String ticker) {
		Boolean tradeReversed = Boolean.FALSE;
		ThetaTrade trade = this.monitoredTrades.get(ticker);
		Double lastPrice = this.callback.getLast(ticker);
		Integer quantity = trade.getEquity().getQuantity();
		Double strikePrice = trade.getStrikePrice();

		this.logger.info("Current tick for '{}': ${}", ticker, lastPrice);

		/*
		 * If position Long, and last price is less than strike price, else if
		 * position Short, and last price is greater than strike price
		 */
		if ((quantity > 0) && (lastPrice < strikePrice)) {
			tradeReversed = this.reversePosition(trade);
		} else if ((quantity < 0) && (lastPrice > strikePrice)) {
			tradeReversed = this.reversePosition(trade);
		}

		return tradeReversed;
	}

	private Boolean reversePosition(ThetaTrade trade) {
		this.logger.info("Reversing position in '{}'}", trade.getBackingTicker());
		this.callback.reverseTrade(trade);
		this.monitoredTrades.put(trade.getBackingTicker(), trade.reversePosition());
		return Boolean.TRUE;
	}
}
