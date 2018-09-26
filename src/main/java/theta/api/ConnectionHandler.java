package theta.api;

import java.time.Instant;
import io.reactivex.Single;
import theta.connection.domain.ConnectionState;

public interface ConnectionHandler {
  Single<Instant> connect();

  Single<Instant> disconnect();

  Single<Instant> waitUntil(ConnectionState waitUntilState);
}
