package theta.util;

import java.time.Instant;

public interface MarketUtility {
    boolean isDuringMarketHours(Instant timeToCheck);
}
