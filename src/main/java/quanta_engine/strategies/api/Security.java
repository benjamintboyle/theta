package quanta_engine.strategies.api;

public interface Security {

	public SecurityType getSecurityType();

	public String getBackingTicker();

	public Integer getQuantity();

	public String toString();
}
