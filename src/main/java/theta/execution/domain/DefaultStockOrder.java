package theta.execution.domain;

import theta.domain.SecurityType;
import theta.domain.Ticker;
import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class DefaultStockOrder implements ExecutableOrder {
    private final UUID id;

    private final Ticker ticker;
    private final long quantity;
    private final ExecutionAction action;
    private final ExecutionType executionType;
    private Double limitPrice = null;
    private Integer brokerId = null;

    /**
     * Create Stock Order.
     */
    public DefaultStockOrder(Ticker ticker, UUID stockId, long quantity, ExecutionAction action, ExecutionType executionType) {
        id = Objects.requireNonNull(stockId, "Stock Id cannot be null");
        this.ticker = Objects.requireNonNull(ticker, "Stock Ticker cannot be null");
        this.quantity = quantity;
        this.action = Objects.requireNonNull(action, "Execution Action cannot be null");
        this.executionType = Objects.requireNonNull(executionType, "Execution Type cannot be null");
    }

    /**
     * Create Stock Order.
     */
    public DefaultStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType) {
        this(stock.getTicker(), stock.getId(), quantity, action, executionType);
    }

    /**
     * Create Stock Order.
     */
    public DefaultStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType, Double limitPrice) {
        this(stock, quantity, action, executionType);
        this.limitPrice = Objects.requireNonNull(limitPrice, "Limit Price cannot be null");
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Optional<Integer> getBrokerId() {
        return Optional.ofNullable(brokerId);
    }

    @Override
    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    @Override
    public Ticker getTicker() {
        return ticker;
    }

    @Override
    public SecurityType getSecurityType() {
        return SecurityType.STOCK;
    }

    @Override
    public long getQuantity() {
        return quantity;
    }

    @Override
    public ExecutionAction getExecutionAction() {
        return action;
    }

    @Override
    public ExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public Optional<Double> getLimitPrice() {
        return Optional.ofNullable(limitPrice);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("[ ");

        builder.append("Ticker: ");
        builder.append(getTicker());
        builder.append(", Action: ");
        builder.append(getExecutionAction());
        builder.append(", Quantity: ");
        builder.append(getQuantity());
        builder.append(", Security Type: ");
        builder.append(getSecurityType());
        builder.append(", Execution Type: ");
        builder.append(getExecutionType());

        final Optional<Double> optionalLimitPrice = getLimitPrice();
        if (getExecutionType() == ExecutionType.LIMIT && optionalLimitPrice.isPresent()) {
            builder.append(", Limit Price: ");
            builder.append(optionalLimitPrice.get());
        }

        builder.append(", Id: ");
        builder.append(getId());
        builder.append(", Broker Id: ");
        builder.append(getBrokerId().orElse(null));

        builder.append(" ]");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultStockOrder that = (DefaultStockOrder) o;
        return quantity == that.quantity &&
                Objects.equals(ticker, that.ticker) &&
                action == that.action &&
                executionType == that.executionType &&
                Objects.equals(limitPrice, that.limitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, quantity, action, executionType, limitPrice);
    }
}
