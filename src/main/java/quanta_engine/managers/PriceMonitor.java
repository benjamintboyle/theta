package quanta_engine.managers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quanta_engine.QuantaEngine;
import quanta_engine.managers.api.Monitor;
import quanta_engine.strategies.ExtrinsicCapture;

public class PriceMonitor implements Monitor {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	QuantaEngine callback;

	private Map<String, ExtrinsicCapture> monitoredTrades = new HashMap<String, ExtrinsicCapture>();

	public PriceMonitor(QuantaEngine callback) {
		this.logger.info("Starting subsystem: 'Monitor'");
		this.callback = callback;
	}

	@Override
	public void addMonitor(ExtrinsicCapture trade) {
		this.logger.info("Adding Monitor for '{}'", trade.getBackingTicker());

		this.monitoredTrades.put(trade.getBackingTicker(), trade);
		this.callback.subscribeMarketData(trade);

	}

	public ExtrinsicCapture deleteMonitor(String ticker) {
		return this.monitoredTrades.remove(ticker);
	}

	// Main monitor method
	public void notifyPriceChange(String ticker) {
		ExtrinsicCapture trade = this.monitoredTrades.get(ticker);
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
		} else if ((quantity < 0) && (lastPrice > strikePrice)) {
			this.logger.info("Reversing Short position in '{}' at price: ${}", ticker, lastPrice);
			this.callback.reverseTrade(trade);
		} else {
			this.logger.info("Current tick for '{}': ${}", ticker, lastPrice);
		}
	}
}
