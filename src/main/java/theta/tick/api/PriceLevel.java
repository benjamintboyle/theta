package theta.tick.api;

public interface PriceLevel {
	public String getTicker();

	public Double getStrikePrice();

	public PriceLevelDirection tradeIf();
}
