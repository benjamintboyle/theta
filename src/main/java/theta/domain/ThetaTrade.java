package theta.domain;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.Security;
import theta.api.SecurityType;

public class ThetaTrade implements Iterable<Security>, Iterator<Security> {
	final Logger logger = LoggerFactory.getLogger(ThetaTrade.class);

	private SecurityType type = SecurityType.THETA;

	private Option call;
	private Option put;
	private Stock equity;

	public ThetaTrade(Security security) {
		this.add(security);
	}

	public ThetaTrade(Security security, Security security2, Security security3) {
		this.add(security);
		this.add(security2);
		this.add(security3);
	}

	public void add(Security security) {
		logger.info("Adding Security: {} to TimeValueCapture: {}", security.toString(), this.toString());

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
			this.logger.error("Invalid Security Type: {}", security.toString());
		}
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

	public Double getStrikePrice() {
		Double strikePrice = 0.0;

		if (this.hasCall()) {
			strikePrice = this.call.getStrikePrice();
		} else if (this.hasPut()) {
			strikePrice = this.put.getStrikePrice();
		} else if (this.hasEquity()) {
			strikePrice = this.equity.getAverageTradePrice();
		} else {
			this.logger.error("Can not determine strike price: {}", this.toString());
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

	public String getBackingTicker() {
		String ticker = null;
		if (this.hasEquity()) {
			ticker = this.equity.getBackingTicker();
		} else if (this.hasCall()) {
			ticker = this.call.getBackingTicker();
		} else if (this.hasPut()) {
			ticker = this.put.getBackingTicker();
		} else {
			this.logger.error("Tried to get backing ticker but not found: {}", this.toString());
		}

		return ticker;
	}

	public ThetaTrade reversePosition() {
		this.getEquity().reversePosition();
		this.logger.info("Reversing trade...");
		return this;
	}

	public List<Security> toSecurityList() {
		return StreamSupport.stream(this.spliterator(), false).collect(Collectors.toList());
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
	public Iterator<Security> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return (this.equity != null) || (this.call != null) || (this.put != null);
	}

	@Override
	public Security next() {
		Security toReturn = null;

		if (this.equity != null) {
			toReturn = this.equity;
		} else if (this.call != null) {
			toReturn = this.call;
		} else if (this.put != null) {
			toReturn = this.put;
		}

		return toReturn;
	}
}
