package theta.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class Option implements Security {
	private static final Logger logger = LoggerFactory.getLogger(Option.class);

	private SecurityType type;
	private String backingTicker = "";
	private Integer quantity;
	private Double strikePrice;
	private LocalDate expiration;

	public Option(SecurityType type, String backingTicker, Integer quantity, Double strikePrice, LocalDate expiration) {
		this.type = type;
		this.backingTicker = backingTicker;
		this.quantity = quantity;
		this.strikePrice = strikePrice;
		this.expiration = expiration;
		logger.info("Built Option: {}", this.toString());
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

	public static LocalDate convertExpiration(String date) {
		LocalDate expiration = null;

		if (date.length() == 6) {
			int year = Integer.parseInt(date.substring(0, 4));
			int month = Integer.parseInt(date.substring(4));
			expiration = Option.convertExpiration(year, month);
		} else if (date.length() == 8) {
			int year = Integer.parseInt(date.substring(0, 4));
			int month = Integer.parseInt(date.substring(4, 6));
			int day = Integer.parseInt(date.substring(6));
			expiration = LocalDate.of(year, month, day);
		}

		return expiration;
	}

	private static LocalDate convertExpiration(int year, int month) {
		// First of the month
		LocalDate expiration = LocalDate.of(year, month, 1);

		// Calculate 3rd Friday by finding first Friday then adding two weeks
		while (!expiration.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
			expiration.plus(1L, ChronoUnit.DAYS);
		}
		expiration.plus(2, ChronoUnit.WEEKS);

		return expiration;
	}

	@Override
	public String toString() {
		String returnString = "{ Type: " + this.getSecurityType();
		returnString += ", Ticker: " + this.getTicker();
		returnString += ", Quantity: " + this.getQuantity();
		returnString += ", Strike: " + this.getStrikePrice();
		returnString += ", Expiration: " + this.getExpiration().toString();
		returnString += " }";

		return returnString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backingTicker == null) ? 0 : backingTicker.hashCode());
		result = prime * result + ((expiration == null) ? 0 : expiration.hashCode());
		result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
		result = prime * result + ((strikePrice == null) ? 0 : strikePrice.hashCode());
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
		Option other = (Option) obj;
		if (backingTicker == null) {
			if (other.backingTicker != null)
				return false;
		} else if (!backingTicker.equals(other.backingTicker))
			return false;
		if (expiration == null) {
			if (other.expiration != null)
				return false;
		} else if (!expiration.equals(other.expiration))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		if (strikePrice == null) {
			if (other.strikePrice != null)
				return false;
		} else if (!strikePrice.equals(other.strikePrice))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
