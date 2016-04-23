package theta.tick.domain;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.portfolio.manager.PortfolioManager;

public class Tick {
	private static final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private final Double price;
	private final String ticker;
	private final ZonedDateTime timestamp;
	private final TickType type;

	public Tick(final String ticker, final Double price, final TickType type, final ZonedDateTime timestamp) {
		this.ticker = ticker;
		this.price = price;
		this.type = type;
		this.timestamp = timestamp;
		Tick.logger.info("Built Tick: {}", this.toString());
	}

	public Double getPrice() {
		return this.price;
	}

	public String getTicker() {
		return this.ticker;
	}

	public TickType getTickType() {
		return this.type;
	}

	public ZonedDateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public String toString() {
		return "Tick [ticker=" + this.ticker + ", price=" + this.price + ", type=" + this.type + ", timestamp="
				+ this.timestamp + "]";
	}
}
