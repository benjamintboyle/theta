package theta.api;

import java.time.ZonedDateTime;
import theta.domain.Ticker;

public interface TickHandler extends PriceLevelMonitor {

  public Ticker getTicker();

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
