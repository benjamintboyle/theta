package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Stock implements Security {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Double averageTradePrice;
  private String backingTicker = "";
  private UUID id = UUID.randomUUID();
  private final Double quantity;
  private final SecurityType type = SecurityType.STOCK;

  public Stock(final UUID id, final String backingTicker, final Double quantity,
      final Double averageTradePrice) {
    this.id = id;
    this.backingTicker = backingTicker;
    this.quantity = quantity;
    this.averageTradePrice = averageTradePrice;
    Stock.logger.info("Built Stock: {}", toString());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final Stock other = (Stock) obj;
    if (averageTradePrice == null) {
      if (other.averageTradePrice != null) {
        return false;
      }
    } else if (!averageTradePrice.equals(other.averageTradePrice)) {
      return false;
    }
    if (backingTicker == null) {
      if (other.backingTicker != null) {
        return false;
      }
    } else if (!backingTicker.equals(other.backingTicker)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (quantity == null) {
      if (other.quantity != null) {
        return false;
      }
    } else if (!quantity.equals(other.quantity)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }

  public Double getAverageTradePrice() {
    return averageTradePrice;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Double getPrice() {
    return getAverageTradePrice();
  }

  @Override
  public Double getQuantity() {
    return quantity;
  }

  @Override
  public SecurityType getSecurityType() {
    return type;
  }

  @Override
  public String getTicker() {
    return backingTicker;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((averageTradePrice == null) ? 0 : averageTradePrice.hashCode());
    result = (prime * result) + ((backingTicker == null) ? 0 : backingTicker.hashCode());
    result = (prime * result) + ((id == null) ? 0 : id.hashCode());
    result = (prime * result) + ((quantity == null) ? 0 : quantity.hashCode());
    result = (prime * result) + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  public Stock reversePosition() {
    Stock.logger.info("Building Reverse of Stock: {}", toString());
    return new Stock(id, backingTicker, -1 * quantity, averageTradePrice);
  }

  @Override
  public String toString() {
    return "Stock [id=" + id + ", type=" + type + ", backingTicker=" + backingTicker + ", quantity="
        + quantity + ", averageTradePrice=" + averageTradePrice + "]";
  }
}
