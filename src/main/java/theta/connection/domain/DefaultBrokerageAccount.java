package theta.connection.domain;

public class DefaultBrokerageAccount implements BrokerageAccount {
  private final String brokerageAccountId;

  public DefaultBrokerageAccount(String brokerageAccountId) {
    this.brokerageAccountId = brokerageAccountId;
  }

  @Override
  public String getId() {
    return brokerageAccountId;
  }

}
