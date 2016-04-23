package theta.domain;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Stock implements Security {
	private static final Logger logger = LoggerFactory.getLogger(Stock.class);

	private final Double averageTradePrice;
	private String backingTicker = "";
	private UUID id = UUID.randomUUID();
	private final Integer quantity;
	private final SecurityType type = SecurityType.STOCK;

	public Stock(final UUID id, final String backingTicker, final Integer quantity, final Double averageTradePrice) {
		this.id = id;
		this.backingTicker = backingTicker;
		this.quantity = quantity;
		this.averageTradePrice = averageTradePrice;
		Stock.logger.info("Built Stock: {}", this.toString());
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
		if (this.averageTradePrice == null) {
			if (other.averageTradePrice != null) {
				return false;
			}
		} else if (!this.averageTradePrice.equals(other.averageTradePrice)) {
			return false;
		}
		if (this.backingTicker == null) {
			if (other.backingTicker != null) {
				return false;
			}
		} else if (!this.backingTicker.equals(other.backingTicker)) {
			return false;
		}
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		if (this.quantity == null) {
			if (other.quantity != null) {
				return false;
			}
		} else if (!this.quantity.equals(other.quantity)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

	public Double getAverageTradePrice() {
		return this.averageTradePrice;
	}

	@Override
	public UUID getId() {
		return this.id;
	}

	@Override
	public Double getPrice() {
		return this.getAverageTradePrice();
	}

	@Override
	public Integer getQuantity() {
		return this.quantity;
	}

	@Override
	public SecurityType getSecurityType() {
		return this.type;
	}

	@Override
	public String getTicker() {
		return this.backingTicker;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.averageTradePrice == null) ? 0 : this.averageTradePrice.hashCode());
		result = (prime * result) + ((this.backingTicker == null) ? 0 : this.backingTicker.hashCode());
		result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
		result = (prime * result) + ((this.quantity == null) ? 0 : this.quantity.hashCode());
		result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	public Stock reversePosition() {
		Stock.logger.info("Building Reverse of Stock: {}", this.toString());
		return new Stock(this.id, this.backingTicker, -1 * this.quantity, this.averageTradePrice);
	}

	@Override
	public String toString() {
		return "Stock [id=" + this.id + ", type=" + this.type + ", backingTicker=" + this.backingTicker + ", quantity="
				+ this.quantity + ", averageTradePrice=" + this.averageTradePrice + "]";
	}
}
