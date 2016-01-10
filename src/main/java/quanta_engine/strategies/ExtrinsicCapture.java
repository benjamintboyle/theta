package quanta_engine.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quanta_engine.strategies.api.Security;
import quanta_engine.strategies.api.SecurityType;

public class ExtrinsicCapture {
	final Logger logger = LoggerFactory.getLogger(ExtrinsicCapture.class);

	private SecurityType type = SecurityType.DCSS;

	private Option call;
	private Option put;
	private Stock equity;

	public ExtrinsicCapture(Security security) {
		this.add(security);
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
}
