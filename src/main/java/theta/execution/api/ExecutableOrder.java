package theta.execution.api;

import theta.domain.SecurityType;
import theta.domain.Ticker;

import java.util.Optional;
import java.util.UUID;

public interface ExecutableOrder {
    UUID getId();

    Ticker getTicker();

    SecurityType getSecurityType();

    ExecutionAction getExecutionAction();

    ExecutionType getExecutionType();

    Optional<Double> getLimitPrice();

    long getQuantity();

    Optional<Integer> getBrokerId();

    void setBrokerId(int orderId);
}
