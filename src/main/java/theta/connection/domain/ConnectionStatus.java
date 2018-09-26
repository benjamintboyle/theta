package theta.connection.domain;

import java.time.Instant;

public class ConnectionStatus {
  private final ConnectionState connectionState;
  private final Instant stateTimestamp;

  private ConnectionStatus(ConnectionState state, Instant time) {
    connectionState = state;
    stateTimestamp = time;
  }

  public static ConnectionStatus of(ConnectionState connectionState) {
    return new ConnectionStatus(connectionState, Instant.now());
  }

  public ConnectionState getState() {
    return connectionState;
  }

  public Instant getTime() {
    return stateTimestamp;
  }
}
