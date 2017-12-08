package theta.api;

public interface ConnectionHandler {
  public Boolean connect();

  public Boolean disconnect();

  public Boolean isConnected();
}
