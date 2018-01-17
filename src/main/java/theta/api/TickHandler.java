package theta.api;

import java.time.ZonedDateTime;

public interface TickHandler extends PriceLevelMonitor {

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
}
