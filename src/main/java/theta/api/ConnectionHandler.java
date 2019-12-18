package theta.api;

import io.reactivex.rxjava3.core.Single;
import java.time.Instant;

public interface ConnectionHandler {
  Single<Instant> connect();

  Single<Instant> disconnect();
}
