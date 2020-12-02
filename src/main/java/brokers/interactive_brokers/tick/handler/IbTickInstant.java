package brokers.interactive_brokers.tick.handler;

import com.ib.client.TickType;

import java.time.Instant;

public interface IbTickInstant {
    TickType getTickType();

    double getInstancePrice();

    Instant getInstantTime();

    void updatePriceTime(double instantPrice, Instant instantTime);
}
