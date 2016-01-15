package theta.managers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.ThetaEngine;
import theta.managers.api.Monitor;
import theta.strategies.ThetaTrade;

public class PriceMonitor implements Monitor {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	ThetaEngine callback;

	private Map<String, ThetaTrade> monitoredTrades = new HashMap<String, ThetaTrade>();

	public PriceMonitor(ThetaEngine callback) {
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

		/*
		 * If position Long, and last price is less than strike price, else if
		 * position Short, and last price is greater than strike price
		 */
		if ((quantity > 0) && (lastPrice < strikePrice)) {
			this.logger.info("Reversing Long position in '{}' at price: ${}", ticker, lastPrice);
			this.callback.reverseTrade(trade);
			this.monitoredTrades.put(ticker, trade.reverseTrade());
			tradeReversed = Boolean.TRUE;
		} else if ((quantity < 0) && (lastPrice > strikePrice)) {
			this.logger.info("Reversing Short position in '{}' at price: ${}", ticker, lastPrice);
			this.callback.reverseTrade(trade);
			this.monitoredTrades.put(ticker, trade.reverseTrade());
			tradeReversed = Boolean.TRUE;
		} else {
			this.logger.info("Current tick for '{}': ${}", ticker, lastPrice);
		}

		return tradeReversed;
	}
}
