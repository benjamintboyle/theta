package theta.api;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import theta.domain.Security;

public interface PositionHandler extends ManagerShutdown {

  Flowable<Security> requestPositionsFromBrokerage();

  Completable getPositionEnd();
}
