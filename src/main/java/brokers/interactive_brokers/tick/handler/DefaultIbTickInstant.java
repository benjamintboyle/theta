package brokers.interactive_brokers.tick.handler;

import com.ib.client.TickType;

import java.time.Instant;

public class DefaultIbTickInstant implements IbTickInstant {

    private final TickType tickType;
    private double instantPrice = -1.0;
    private Instant instantTime = Instant.EPOCH;

    public DefaultIbTickInstant(TickType tickType) {
        this.tickType = tickType;
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }

    @Override
    public double getInstancePrice() {
        return instantPrice;
    }

    @Override
    public Instant getInstantTime() {
        return instantTime;
    }

    @Override
    public void updatePriceTime(double instantPrice, Instant instantTime) {
        this.instantPrice = instantPrice;
        this.instantTime = instantTime;
    }
}
