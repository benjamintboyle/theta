package theta.tick.domain;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.portfolio.manager.PortfolioManager;

public class Tick {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private String ticker;
	private Double price;
	private TickType type;
	private ZonedDateTime timestamp;

	public Tick(String ticker, Double price, TickType type, ZonedDateTime timestamp) {
		this.ticker = ticker;
		this.price = price;
		this.type = type;
		this.timestamp = timestamp;
		logger.info("Built Tick: {}", this.toString());
	}

	public String getTicker() {
		return this.ticker;
	}

	public Double getPrice() {
		return this.price;
	}

	public TickType getTickType() {
		return this.type;
	}

	public ZonedDateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public String toString() {
		return "Tick [ticker=" + ticker + ", price=" + price + ", type=" + type + ", timestamp=" + timestamp + "]";
	}
}
