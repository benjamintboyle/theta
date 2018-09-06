package theta.portfolio.api;

import java.util.List;
import theta.domain.Ticker;
import theta.domain.composed.Theta;

public interface PositionProvider {
  public List<Theta> providePositions(Ticker ticker);
}
