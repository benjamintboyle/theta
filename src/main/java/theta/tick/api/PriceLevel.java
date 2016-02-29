package theta.tick.api;

public interface PriceLevel {
	public String getBackingTicker();

	public Double getStrikePrice();

	public PriceLevelDirection tradeIf();
}
