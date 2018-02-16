package theta.domain.api;

import java.util.UUID;
import theta.domain.Ticker;

public interface Security {

  public UUID getId();

  public SecurityType getSecurityType();

  public Ticker getTicker();

  public long getQuantity();

  public double getPrice();

  @Override
  public String toString();
}
