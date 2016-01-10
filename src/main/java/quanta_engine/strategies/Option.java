package quanta_engine.strategies;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import quanta_engine.strategies.api.Security;
import quanta_engine.strategies.api.SecurityType;

public class Option implements Security {
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

	public Double getStrikePrice() {
		return this.strikePrice;
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
		returnString += ", Ticker: " + this.getBackingTicker();
		returnString += ", Quantity: " + this.getQuantity();
		returnString += ", Strike: " + this.getStrikePrice();
		returnString += ", Expiration: " + this.getExpiration().toString();
		returnString += " }";

		return returnString;
	}
}
