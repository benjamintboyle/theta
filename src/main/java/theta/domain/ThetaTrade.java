package theta.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.domain.api.Security;
import theta.domain.api.SecurityType;
import theta.tick.api.PriceLevel;
import theta.tick.api.PriceLevelDirection;

public class ThetaTrade implements PriceLevel {
	private static final Logger logger = LoggerFactory.getLogger(ThetaTrade.class);

	private final SecurityType type = SecurityType.THETA;

	private Option call;
	private Option put;
	private Stock equity;
	private Integer quantity = 0;

	private ThetaTrade(Stock stock, Option call, Option put) {
		logger.info("Building Theta: {}, {}, {}", stock, call, put);
		this.add(call);
		this.add(put);
		this.add(stock);
	}

	public static Optional<ThetaTrade> of(Stock stock, Option call, Option put) {
		ThetaTrade theta = null;

		// All same ticker
		if ((stock.getTicker().equals(call.getTicker())) && (call.getTicker().equals(put.getTicker()))) {
			// Options have same strike
			if (call.getStrikePrice().equals(put.getStrikePrice())) {
				// Options have same expiration
				if (call.getExpiration().equals(put.getExpiration())) {
					// If quantities match
					if ((call.getQuantity().equals(put.getQuantity()))
							&& (put.getQuantity().equals(stock.getQuantity() / -100))) {
						// Options are opposite types
						if (call.getSecurityType().equals(SecurityType.CALL)
								&& put.getSecurityType().equals(SecurityType.PUT)) {
							theta = new ThetaTrade(stock, call, put);
						} else {
							logger.error("Options aren't a call: {} and a put: {}", call, put);
						}
					} else {
						logger.error("Quantities do not match: {}, {}, {}", stock, call, put);
					}
				} else {
					logger.error("Option expirations do not match: {}, {}", call, put);
				}
			} else {
				logger.error("Option strike prices do not match: {}, {}", call, put);
			}
		} else {
			logger.error("Tickers do not match: {}, {}, {}", stock, call, put);
		}

		return Optional.ofNullable(theta);
	}

	private void add(Security security) {
		logger.info("Adding Security: {} to ThetaTrade: {}", security.toString(), this.toString());

		if ((!this.hasEquity() && !this.hasOption())
				|| (!this.isComplete() && security.getTicker().equals(this.getTicker()))) {
			switch (security.getSecurityType()) {
			case STOCK:
				this.equity = (Stock) security;
				break;
			case CALL:
				this.call = (Option) security;
				break;
			case PUT:
				this.put = (Option) security;
				break;
			default:
				logger.error("Invalid Security Type: {}", security.toString());
			}

			if (this.isComplete()) {
				this.quantity++;
			}
		} else {
			logger.error("Trying to add Security: {} to invalid Theta: {}", security, this);
		}
	}

	public void add(ThetaTrade theta) {
		if (this.getTicker().equals(theta.getTicker())) {
			if (theta.getStrikePrice().equals(this.getStrikePrice()) || this.quantity == 0) {
				this.quantity += theta.getQuantity();
			} else {
				logger.error("Tried adding incompatible strike prices: {} to this {}", theta, this);
			}
		} else {
			logger.error("Tried combine different tickers: {} to this {}", theta, this);
		}
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public Stock getEquity() {
		return this.equity;
	}

	public Option getCall() {
		return this.call;
	}

	public Option getPut() {
		return this.put;
	}

	@Override
	public Double getStrikePrice() {
		Double strikePrice = 0.0;

		if (this.hasCall()) {
			strikePrice = this.call.getStrikePrice();
		} else if (this.hasPut()) {
			strikePrice = this.put.getStrikePrice();
		} else if (this.hasEquity()) {
			strikePrice = this.equity.getAverageTradePrice();
		} else {
			logger.error("Can not determine strike price: {}", this.toString());
		}

		return strikePrice;
	}

	public Boolean isComplete() {
		return this.isOptionComplete() && this.hasEquity();
	}

	public Boolean isOptionComplete() {
		return this.hasCall() && this.hasPut();
	}

	public Boolean hasOption() {
		return this.hasCall() || this.hasPut();
	}

	public Boolean hasOption(Security security) {
		if (security.getSecurityType().equals(SecurityType.CALL) && this.hasCall()) {
			return Boolean.TRUE;
		}
		if (security.getSecurityType().equals(SecurityType.PUT) && this.hasPut()) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public Boolean hasCall() {
		return this.call != null;
	}

	public Boolean hasPut() {
		return this.put != null;
	}

	public Boolean hasEquity() {
		return this.equity != null;
	}

	public SecurityType getStrategyType() {
		return this.type;
	}

	@Override
	public String getTicker() {
		String ticker = null;
		if (this.hasEquity()) {
			ticker = this.equity.getTicker();
		} else if (this.hasCall()) {
			ticker = this.call.getTicker();
		} else if (this.hasPut()) {
			ticker = this.put.getTicker();
		} else {
			logger.error("Tried to get backing ticker but not found: {}", this.toString());
		}

		return ticker;
	}

	public ThetaTrade reversePosition() {
		this.getEquity().reversePosition();
		logger.info("Reversing trade...");
		return this;
	}

	public List<Security> toSecurityList() {
		List<Security> securityList = new ArrayList<Security>();
		securityList.add(this.getEquity());
		securityList.add(this.getCall());
		securityList.add(this.getPut());

		return securityList;
	}

	@Override
	public String toString() {
		String returnString = "{ Type: " + this.getStrategyType();
		if (this.hasCall()) {
			returnString += ", " + this.call.getSecurityType() + ": " + this.call.toString();
		}
		if (this.hasPut()) {
			returnString += ", " + this.put.getSecurityType() + ": " + this.put.toString();
		}
		if (this.hasEquity()) {
			returnString += ", " + this.equity.getSecurityType() + ": " + this.equity.toString();
		}
		returnString += " }";

		return returnString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((call == null) ? 0 : call.hashCode());
		result = prime * result + ((equity == null) ? 0 : equity.hashCode());
		result = prime * result + ((put == null) ? 0 : put.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThetaTrade other = (ThetaTrade) obj;
		if (call == null) {
			if (other.call != null)
				return false;
		} else if (!call.equals(other.call))
			return false;
		if (equity == null) {
			if (other.equity != null)
				return false;
		} else if (!equity.equals(other.equity))
			return false;
		if (put == null) {
			if (other.put != null)
				return false;
		} else if (!put.equals(other.put))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public PriceLevelDirection tradeIf() {
		PriceLevelDirection priceLevelDirection = PriceLevelDirection.FALLS_BELOW;

		if (this.getEquity().getQuantity() > 0) {
			priceLevelDirection = PriceLevelDirection.FALLS_BELOW;
		}
		if (this.getEquity().getQuantity() < 0) {
			priceLevelDirection = PriceLevelDirection.RISES_ABOVE;
		}

		return priceLevelDirection;
	}
}
