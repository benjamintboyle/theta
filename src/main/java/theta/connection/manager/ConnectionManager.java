package theta.connection.manager;

import java.time.Instant;
import io.reactivex.Single;
import theta.api.ManagerShutdown;

public interface ConnectionManager extends ManagerShutdown {

  Single<Instant> connect();
}
