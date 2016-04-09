package theta.api;

import java.time.ZonedDateTime;

import theta.tick.api.PriceLevel;

public interface TickHandler {

	public String getTicker();

	public Double getBid();

	public Double getAsk();

	public Double getLast();

	public ZonedDateTime getLastTime();

	public Integer getBidSize();

	public Integer getAskSize();

	public Double getClose();

	public Integer getVolume();

	public Boolean isSnapshot();

	public Integer addPriceLevel(PriceLevel priceLevel);

	public Integer removePriceLevel(PriceLevel priceLevel);
}
