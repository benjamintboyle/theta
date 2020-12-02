package theta.domain;

import java.util.UUID;

public interface Security {
    UUID getId();

    SecurityType getSecurityType();

    Ticker getTicker();

    long getQuantity();

    double getPrice();
}
