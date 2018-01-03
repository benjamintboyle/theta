package theta.connection.domain;

import java.time.ZonedDateTime;

public class ConnectionStatus {
  private final ConnectionState connectionState;
  private final ZonedDateTime stateTimestamp;

  private ConnectionStatus(ConnectionState state, ZonedDateTime time) {
    connectionState = state;
    stateTimestamp = time;
  }

  public static ConnectionStatus of(ConnectionState connectionState) {
    return new ConnectionStatus(connectionState, ZonedDateTime.now());
  }

  public ConnectionState getState() {
    return connectionState;
  }

  public ZonedDateTime getTime() {
    return stateTimestamp;
  }
}
