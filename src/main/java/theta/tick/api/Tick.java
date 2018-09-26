package theta.tick.api;

import java.time.Instant;
import theta.domain.Ticker;
import theta.tick.domain.TickType;

public interface Tick {

  Double getLastPrice();

  Double getBidPrice();

  Double getAskPrice();

  Ticker getTicker();

  TickType getTickType();

  Instant getTimestamp();
}
