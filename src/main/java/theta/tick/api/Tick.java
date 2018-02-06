package theta.tick.api;

import java.time.ZonedDateTime;
import theta.domain.Ticker;
import theta.tick.domain.TickType;

public interface Tick {

  public Double getLastPrice();

  public Double getBidPrice();

  public Double getAskPrice();

  public Ticker getTicker();

  public TickType getTickType();

  public ZonedDateTime getTimestamp();
}
