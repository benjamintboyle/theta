package theta.tick.domain;

import java.time.LocalDateTime;

public class Tick {
	private String ticker;
	private Double price;
	private TickType type;
	private LocalDateTime timestamp;

	public Tick(String ticker, Double price, TickType type, LocalDateTime timestamp) {
		this.ticker = ticker;
		this.price = price;
		this.type = type;
		this.timestamp = timestamp;
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
