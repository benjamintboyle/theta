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
	private SecurityType type;
	private String backingTicker;
	private Integer quantity;
	private Double strikePrice;
	private LocalDate expiration;
	private Double averageTradePrice;

	public Option(UUID id, SecurityType type, String backingTicker, Integer quantity, Double strikePrice,
			LocalDate expiration, Double averageTradePrice) {
		this.id = id;
		this.type = type;
		this.backingTicker = backingTicker;
		this.quantity = quantity;
		this.strikePrice = strikePrice;
		this.expiration = expiration;
		this.averageTradePrice = averageTradePrice;
		logger.info("Built Option: {}", this.toString());
	}

	@Override
	public UUID getId() {
		return this.id;
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
	public Integer getQuantity() {
		return this.quantity;
	}

	public Double getStrikePrice() {
		return this.strikePrice;
	}

	@Override
	public Double getPrice() {
		return this.getStrikePrice();
	}

	public LocalDate getExpiration() {
		return this.expiration;
	}

	public Double getAverageTradePrice() {
		return this.averageTradePrice;
	}

	@Override
	public String toString() {
		return "Option [id=" + this.id + ", type=" + this.type + ", backingTicker=" + this.backingTicker + ", quantity="
				+ this.quantity + ", strikePrice=" + this.strikePrice + ", expiration=" + this.expiration
				+ ", averageTradePrice=" + this.averageTradePrice + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.averageTradePrice == null) ? 0 : this.averageTradePrice.hashCode());
		result = prime * result + ((this.backingTicker == null) ? 0 : this.backingTicker.hashCode());
		result = prime * result + ((this.expiration == null) ? 0 : this.expiration.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.quantity == null) ? 0 : this.quantity.hashCode());
		result = prime * result + ((this.strikePrice == null) ? 0 : this.strikePrice.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
		Option other = (Option) obj;
		if (this.averageTradePrice == null) {
			if (other.averageTradePrice != null)
				return false;
		} else if (!this.averageTradePrice.equals(other.averageTradePrice))
			return false;
		if (this.backingTicker == null) {
			if (other.backingTicker != null)
				return false;
		} else if (!this.backingTicker.equals(other.backingTicker))
			return false;
		if (this.expiration == null) {
			if (other.expiration != null)
				return false;
		} else if (!this.expiration.equals(other.expiration))
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!this.quantity.equals(other.quantity))
			return false;
		if (this.strikePrice == null) {
			if (other.strikePrice != null)
				return false;
		} else if (!this.strikePrice.equals(other.strikePrice))
			return false;
		if (this.type != other.type)
			return false;
		return true;
	}
}
