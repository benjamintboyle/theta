package theta.domain;

import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Option implements Security {
  private static final Logger logger = LoggerFactory.getLogger(Option.class);

  private UUID id = UUID.randomUUID();
  private final SecurityType type;
  private final String backingTicker;
  private final Double quantity;
  private final Double strikePrice;
  private final LocalDate expiration;
  private final Double averageTradePrice;

  public Option(UUID id, SecurityType type, String backingTicker, Double quantity,
      Double strikePrice, LocalDate expiration, Double averageTradePrice) {
    this.id = id;
    this.type = type;
    this.backingTicker = backingTicker;
    this.quantity = quantity;
    this.strikePrice = strikePrice;
    this.expiration = expiration;
    this.averageTradePrice = averageTradePrice;
    logger.info("Built Option: {}", toString());
  }

  @Override
  public UUID getId() {
    return id;
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
  public Double getQuantity() {
    return quantity;
  }

  public Double getStrikePrice() {
    return strikePrice;
  }

  @Override
  public Double getPrice() {
    return getStrikePrice();
  }

  public LocalDate getExpiration() {
    return expiration;
  }

  public Double getAverageTradePrice() {
    return averageTradePrice;
  }

  @Override
  public String toString() {
    return "Option [id=" + id + ", type=" + type + ", backingTicker=" + backingTicker
        + ", quantity=" + quantity + ", strikePrice=" + strikePrice + ", expiration=" + expiration
        + ", averageTradePrice=" + averageTradePrice + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((averageTradePrice == null) ? 0 : averageTradePrice.hashCode());
    result = prime * result + ((backingTicker == null) ? 0 : backingTicker.hashCode());
    result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
    result = prime * result + ((strikePrice == null) ? 0 : strikePrice.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Option other = (Option) obj;
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
    if (expiration == null) {
      if (other.expiration != null) {
        return false;
      }
    } else if (!expiration.equals(other.expiration)) {
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
    if (strikePrice == null) {
      if (other.strikePrice != null) {
        return false;
      }
    } else if (!strikePrice.equals(other.strikePrice)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }
}
