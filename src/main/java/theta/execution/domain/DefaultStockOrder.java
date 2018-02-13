package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Ticker;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;

public class DefaultStockOrder implements ExecutableOrder {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id;

  private final Ticker ticker;
  private final long quantity;
  private final ExecutionAction action;
  private final ExecutionType executionType;
  private Optional<Double> limitPrice = Optional.empty();
  private Optional<Integer> brokerId = Optional.empty();

  public DefaultStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType) {
    id = Objects.requireNonNull(stock.getId(), "Stock Id cannot be null");
    ticker = Objects.requireNonNull(stock.getTicker(), "Stock Ticker cannot be null");
    this.quantity = quantity;
    this.action = Objects.requireNonNull(action, "Execution Action cannot be null");
    this.executionType = Objects.requireNonNull(executionType, "Execution Type cannot be null");

    logger.debug("Built: {}", toString());
  }

  public DefaultStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType,
      Double limitPrice) {
    this(stock, quantity, action, executionType);

    this.limitPrice = Optional.of(Objects.requireNonNull(limitPrice, "Limit Price cannot be null"));

    logger.debug("Built: {}", toString());
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Optional<Integer> getBrokerId() {
    return brokerId;
  }

  @Override
  public void setBrokerId(Integer brokerId) {
    this.brokerId = Optional.ofNullable(brokerId);
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
    return limitPrice;
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();

    builder.append("Stock Order [");

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

    if (getExecutionType() == ExecutionType.LIMIT && getLimitPrice().isPresent()) {
      builder.append(", Limit Price: ");
      builder.append(getLimitPrice().get());
    }

    builder.append(", Id: ");
    builder.append(getId());
    builder.append(", Broker Id: ");
    builder.append(getBrokerId().orElse(null));

    builder.append("]");

    return builder.toString();
  }

  @Override
  public int hashCode() {

    return Objects.hash(getTicker(), getQuantity(), getExecutionAction(), getSecurityType(), getSecurityType(),
        getExecutionType(), getLimitPrice());
  }

  @Override
  public boolean equals(Object obj) {
    boolean isEqual = false;

    if (obj == this) {
      isEqual = true;
    }

    if (obj instanceof DefaultStockOrder) {
      final DefaultStockOrder other = (DefaultStockOrder) obj;

      isEqual = Objects.equals(getTicker(), other.getTicker()) && Objects.equals(getQuantity(), other.getQuantity())
          && Objects.equals(getExecutionAction(), other.getExecutionAction())
          && Objects.equals(getSecurityType(), other.getSecurityType())
          && Objects.equals(getExecutionType(), other.getExecutionType())
          && Objects.equals(getLimitPrice(), other.getLimitPrice());
    }

    return isEqual;
  }

}