package theta.tick.processor;

import java.util.Optional;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.execution.domain.ExecutionType;
import theta.tick.api.Tick;
import theta.tick.domain.TickType;

public interface TickProcessor {

  public boolean isApplicable(TickType tickType);

  public boolean process(Tick tick, PriceLevel priceLevel);

  public ExecutionType getExecutionType();

  public Optional<Double> getLimitPrice(Ticker ticker);

}
