package theta.connection.manager;

import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import theta.api.ManagerShutdown;

public interface ConnectionManager extends ManagerShutdown {

  Single<Instant> connect();
}
