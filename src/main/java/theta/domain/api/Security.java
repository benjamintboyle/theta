package theta.domain.api;

import java.util.UUID;

public interface Security {

  public UUID getId();

  public SecurityType getSecurityType();

  public String getTicker();

  public Double getQuantity();

  public Double getPrice();

  @Override
  public String toString();
}
