package theta.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.NewContract;

import theta.api.Security;
import theta.api.SecurityType;

public class Stock implements Security {
	private final Logger logger = LoggerFactory.getLogger(Stock.class);

	private final SecurityType type = SecurityType.STOCK;
	private String backingTicker = "";
	private Integer quantity;
	private Double averageTradePrice;

	// TODO this needs to be removed and replaced with non-implementation
	// specific data structure
	private NewContract contract;

	// TODO Remove NewContract
	public Stock(String backingTicker, Integer quantity, Double averageTradePrice, NewContract contract) {
		this.backingTicker = backingTicker;
		this.quantity = quantity;
		this.averageTradePrice = averageTradePrice;
		this.contract = contract;
		logger.info("Built Stock: {}", this.toString());
	}

	@Override
	public SecurityType getSecurityType() {
		return this.type;
	}

	@Override
	public String getBackingTicker() {
		return this.backingTicker;
	}

	@Override
	public Integer getQuantity() {
		return this.quantity;
	}

	public Double getAverageTradePrice() {
		return this.averageTradePrice;
	}

	// TODO Replace with non-implementation specific (i.e. not IB)
	public NewContract getContract() {
		return this.contract;
	}

	public Stock reversePosition() {
		logger.info("Building Reverse of Stock: {}", this.toString());
		return new Stock(this.backingTicker, -1 * this.quantity, this.averageTradePrice, this.contract);
	}

	@Override
	public String toString() {
		String returnString = "{ Type: " + this.getSecurityType();
		returnString += ", Ticker: " + this.getBackingTicker();
		returnString += ", Quantity: " + this.getQuantity();
		returnString += ", Avg Price: " + this.getAverageTradePrice();
		returnString += " }";

		return returnString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((averageTradePrice == null) ? 0 : averageTradePrice.hashCode());
		result = prime * result + ((backingTicker == null) ? 0 : backingTicker.hashCode());
		result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
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
		Stock other = (Stock) obj;
		if (averageTradePrice == null) {
			if (other.averageTradePrice != null)
				return false;
		} else if (!averageTradePrice.equals(other.averageTradePrice))
			return false;
		if (backingTicker == null) {
			if (other.backingTicker != null)
				return false;
		} else if (!backingTicker.equals(other.backingTicker))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
