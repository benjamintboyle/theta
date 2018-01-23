package theta.domain.api;

import java.util.UUID;
import theta.domain.Ticker;

public interface Security {

  public UUID getId();

  public SecurityType getSecurityType();

  public Ticker getTicker();

  public Double getQuantity();

  public Double getPrice();

  @Override
  public String toString();
}
