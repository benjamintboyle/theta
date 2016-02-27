package theta.tick.domain;

public class Tick {
	private String ticker;
	private Double price;
	private TickType type;

	public Tick(String ticker, Double price, TickType type) {
		this.ticker = ticker;
		this.price = price;
		this.type = type;
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
}
