package theta.execution.domain;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Stock;
import theta.domain.Ticker;
import theta.domain.api.SecurityType;
import theta.execution.api.ExecutableOrder;

public abstract class AbstractStockOrder implements ExecutableOrder {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id;

  private final Ticker ticker;
  private final long quantity;
  private final ExecutionAction action;
  private final ExecutionType executionType;
  private Optional<Double> limitPrice = Optional.empty();
  private Optional<Integer> brokerId = Optional.empty();

  public AbstractStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType) {
    id = stock.getId();
    ticker = stock.getTicker();
    this.quantity = quantity;
    this.action = action;
    this.executionType = executionType;

    logger.debug("Built: {}", toString());
  }

  public AbstractStockOrder(Stock stock, long quantity, ExecutionAction action, ExecutionType executionType,
      Double limitPrice) {
    this(stock, quantity, action, executionType);

    this.limitPrice = Optional.of(limitPrice);

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
    builder.append(", Id: ");
    builder.append(getId());
    builder.append(", Broker Id: ");
    builder.append(getBrokerId().orElse(null));

    builder.append("]");

    return builder.toString();
  }
}
