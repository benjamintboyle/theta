package theta.domain;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.tick.api.PriceLevel;
import theta.tick.api.PriceLevelDirection;

public class ThetaTrade implements PriceLevel {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UUID id = UUID.randomUUID();
  private final SecurityType type = SecurityType.THETA;
  private Option call;
  private Option put;
  private Stock equity;
  private Integer quantity = 0;

  private ThetaTrade(UUID id, Stock stock, Option call, Option put) {
    this.add(call);
    this.add(put);
    this.add(stock);
    logger.info("Built Theta: {}", this);
  }

  public UUID getId() {
    return id;
  }

  public static Optional<ThetaTrade> of(Stock stock, Option call, Option put) {
    ThetaTrade theta = null;

    // All same ticker
    if ((stock.getTicker().equals(call.getTicker()))
        && (call.getTicker().equals(put.getTicker()))) {
      // Options have same strike
      if (call.getStrikePrice().equals(put.getStrikePrice())) {
        // Options have same expiration
        if (call.getExpiration().equals(put.getExpiration())) {
          // If quantities match
          if ((call.getQuantity() != 0) && (call.getQuantity().equals(put.getQuantity()))
              && (Math.abs(stock.getQuantity() / call.getQuantity()) == 100)) {
            // Options are opposite types
            if (call.getSecurityType().equals(SecurityType.CALL)
                && put.getSecurityType().equals(SecurityType.PUT)) {
              theta = new ThetaTrade(UUID.randomUUID(), stock, call, put);
            } else {
              logger.warn("Options aren't a call: {} and a put: {}", call, put);
            }
          } else {
            logger.warn("Quantities do not match: {}, {}, {}", stock, call, put);
          }
        } else {
          logger.warn("Option expirations do not match: {}, {}", call, put);
        }
      } else {
        logger.warn("Option strike prices do not match: {}, {}", call, put);
      }
    } else {
      logger.warn("Tickers do not match: {}, {}, {}", stock, call, put);
    }

    return Optional.ofNullable(theta);
  }

  private void add(Security security) {
    logger.info("Adding Security: {} to ThetaTrade: {}", security.toString(), toString());

    if ((!hasEquity() && !this.hasOption())
        || (!isComplete() && security.getTicker().equals(getTicker()))) {
      switch (security.getSecurityType()) {
        case STOCK:
          equity = (Stock) security;
          break;
        case CALL:
          call = (Option) security;
          break;
        case PUT:
          put = (Option) security;
          break;
        default:
          logger.error("Invalid Security Type: {}", security.toString());
      }

      if (isComplete()) {
        quantity++;
      }
    } else {
      logger.error("Trying to add Security: {} to invalid Theta: {}", security, this);
    }
  }

  public void add(ThetaTrade theta) {
    if (getTicker().equals(theta.getTicker())) {
      if (theta.getStrikePrice().equals(getStrikePrice()) || quantity == 0) {
        quantity += theta.getQuantity();
      } else {
        logger.error("Tried adding incompatible strike prices: {} to this {}", theta, this);
      }
    } else {
      logger.error("Tried combine different tickers: {} to this {}", theta, this);
    }
  }

  public Integer getQuantity() {
    return quantity;
  }

  public Stock getEquity() {
    return equity;
  }

  public Option getCall() {
    return call;
  }

  public Option getPut() {
    return put;
  }

  @Override
  public Double getStrikePrice() {
    Double strikePrice = 0.0;

    if (hasCall()) {
      strikePrice = call.getStrikePrice();
    } else if (hasPut()) {
      strikePrice = put.getStrikePrice();
    } else if (hasEquity()) {
      strikePrice = equity.getAverageTradePrice();
    } else {
      logger.error("Can not determine strike price: {}", toString());
    }

    return strikePrice;
  }

  public Boolean isComplete() {
    return isOptionComplete() && hasEquity();
  }

  public Boolean isOptionComplete() {
    return hasCall() && hasPut();
  }

  public Boolean hasOption() {
    return hasCall() || hasPut();
  }

  public Boolean hasOption(Security security) {
    if (security.getSecurityType().equals(SecurityType.CALL) && hasCall()) {
      return Boolean.TRUE;
    }
    if (security.getSecurityType().equals(SecurityType.PUT) && hasPut()) {
      return Boolean.TRUE;
    }

    return Boolean.FALSE;
  }

  public Boolean hasCall() {
    return call != null;
  }

  public Boolean hasPut() {
    return put != null;
  }

  public Boolean hasEquity() {
    return equity != null;
  }

  public SecurityType getStrategyType() {
    return type;
  }

  @Override
  public String getTicker() {
    String ticker = null;
    if (hasEquity()) {
      ticker = equity.getTicker();
    } else if (hasCall()) {
      ticker = call.getTicker();
    } else if (hasPut()) {
      ticker = put.getTicker();
    } else {
      logger.error("Tried to get backing ticker but not found: {}", toString());
    }

    return ticker;
  }

  public ThetaTrade reversePosition() {
    final Stock stock = getEquity().reversePosition();
    final ThetaTrade reversedTheta = new ThetaTrade(id, stock, call, put);
    logger.info("Reversing trade from {}, to this {}", this, reversedTheta);
    return reversedTheta;
  }

  public List<Security> toSecurityList() {
    final List<Security> securityList = new ArrayList<Security>();
    securityList.add(getEquity());
    securityList.add(getCall());
    securityList.add(getPut());

    return securityList;
  }

  @Override
  public PriceLevelDirection tradeIf() {
    PriceLevelDirection priceLevelDirection = PriceLevelDirection.FALLS_BELOW;

    if (getEquity().getQuantity() > 0) {
      priceLevelDirection = PriceLevelDirection.FALLS_BELOW;
    }
    if (getEquity().getQuantity() < 0) {
      priceLevelDirection = PriceLevelDirection.RISES_ABOVE;
    }

    return priceLevelDirection;
  }

  @Override
  public String toString() {
    return "ThetaTrade [id=" + id + ", type=" + type + ", quantity=" + quantity + ", equity="
        + equity + ", call=" + call + ", put=" + put + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((call == null) ? 0 : call.hashCode());
    result = prime * result + ((equity == null) ? 0 : equity.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((put == null) ? 0 : put.hashCode());
    result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
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
    final ThetaTrade other = (ThetaTrade) obj;
    if (call == null) {
      if (other.call != null) {
        return false;
      }
    } else if (!call.equals(other.call)) {
      return false;
    }
    if (equity == null) {
      if (other.equity != null) {
        return false;
      }
    } else if (!equity.equals(other.equity)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (put == null) {
      if (other.put != null) {
        return false;
      }
    } else if (!put.equals(other.put)) {
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
}
