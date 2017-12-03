package theta.connection.manager;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.api.ConnectionHandler;

public class ConnectionManager {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ConnectionHandler connectionHandler;

  public ConnectionManager(ConnectionHandler connectionHandler) {
    logger.info("Starting Connection Manager");
    this.connectionHandler = connectionHandler;
  }

  public void connect() {
    logger.info("Connecting to Broker servers...");
    connectionHandler.connect();
  }

  public void shutdown() {
    logger.info("Shutting down 'Connection Manager' subsystem");
    connectionHandler.disconnect();
  }
}
