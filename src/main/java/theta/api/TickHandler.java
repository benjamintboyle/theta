package theta.api;

import java.time.LocalDateTime;

import theta.tick.domain.Tick;

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

	public Tick getTick();
}
