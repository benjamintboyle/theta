package theta.portfolio.api;

import java.util.List;
import theta.domain.ThetaTrade;

public interface PositionProvider {
  public List<ThetaTrade> providePositions(String ticker);
}
