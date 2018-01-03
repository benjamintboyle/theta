package theta.api;

import java.time.ZonedDateTime;
import io.reactivex.Single;
import theta.connection.domain.ConnectionState;

public interface ConnectionHandler {
  public Single<ZonedDateTime> connect();

  public Single<ZonedDateTime> disconnect();

  public Single<ZonedDateTime> waitUntil(ConnectionState waitUntilState);
}
