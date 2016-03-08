package theta.tick.domain;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.portfolio.manager.PortfolioManager;

public class Tick {
	private final Logger logger = LoggerFactory.getLogger(PortfolioManager.class);

	private String ticker;
	private Double price;
	private TickType type;
	private LocalDateTime timestamp;

	public Tick(String ticker, Double price, TickType type, LocalDateTime timestamp) {
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

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}
}
