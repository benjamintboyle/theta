package theta.api;

import io.reactivex.Completable;
import theta.portfolio.api.PortfolioObserver;

public interface PositionHandler {
  public void subscribePositions(PortfolioObserver observer);

  public Completable requestPositionsFromBrokerage();

  public Completable getPositionEnd();
}
