package theta.domain;

import com.ib.controller.NewContract;

import theta.api.Security;
import theta.api.SecurityType;

public class Stock implements Security {
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
		this.quantity *= -1;
		return this;
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
}
