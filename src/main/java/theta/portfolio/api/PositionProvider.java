package theta.portfolio.api;

import java.util.List;
import theta.api.ManagerShutdown;
import theta.domain.Ticker;
import theta.domain.composed.Theta;

public interface PositionProvider extends ManagerShutdown {
  List<Theta> providePositions(Ticker ticker);
}
