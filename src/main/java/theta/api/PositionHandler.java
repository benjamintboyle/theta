package theta.api;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import theta.domain.api.Security;

public interface PositionHandler {

  public Flowable<Security> requestPositionsFromBrokerage();

  public Completable getPositionEnd();
}
