package theta.portfolio.api;

import java.util.List;
import theta.domain.Theta;
import theta.domain.Ticker;

public interface PositionProvider {
  public List<Theta> providePositions(Ticker ticker);
}
