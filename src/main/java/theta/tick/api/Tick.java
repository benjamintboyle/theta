package theta.tick.api;

import theta.domain.Ticker;
import theta.tick.domain.TickType;

import java.time.Instant;

public interface Tick {
    double getLastPrice();

    double getBidPrice();

    double getAskPrice();

    Ticker getTicker();

    TickType getTickType();

    Instant getTimestamp();
}
