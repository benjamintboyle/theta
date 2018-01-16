package theta.portfolio.api;

import java.util.List;
import theta.domain.Theta;

public interface PositionProvider {
  public List<Theta> providePositions(String ticker);
}
