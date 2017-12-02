package theta.domain.api;

import java.util.UUID;

public interface Security {

  public UUID getId();

  public SecurityType getSecurityType();

  public String getTicker();

  public Double getQuantity();

  @Override
  public String toString();

  public Double getPrice();
}
