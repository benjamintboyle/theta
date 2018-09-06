package theta.tick.api;

import java.util.Optional;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.execution.api.ExecutionType;
import theta.tick.domain.TickType;

public interface TickProcessor {

  public boolean isApplicable(TickType tickType);

  public boolean processTick(Tick tick, PriceLevel priceLevel);

  public ExecutionType getExecutionType();

  public Optional<Double> getLimitPrice(Ticker ticker);

}
