package theta.domain;

import java.util.UUID;

public interface Security {

  public UUID getId();

  public SecurityType getSecurityType();

  public Ticker getTicker();

  public long getQuantity();

  public double getPrice();

  @Override
  public String toString();
}
