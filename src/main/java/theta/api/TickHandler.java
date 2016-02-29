package theta.api;

import java.time.LocalDateTime;

import theta.tick.api.PriceLevel;

public interface TickHandler {
	public String getTicker();

	public Double getBid();

	public Double getAsk();

	public Double getLast();

	public LocalDateTime getLastTime();

	public Integer getBidSize();

	public Integer getAskSize();

	public Double getClose();

	public Integer getVolume();

	public Boolean isSnapshot();

	public void addPriceLevel(PriceLevel priceLevel);

	public void removePriceLevel(PriceLevel priceLevel);
}
