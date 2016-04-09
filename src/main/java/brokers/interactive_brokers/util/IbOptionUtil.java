package brokers.interactive_brokers.util;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbOptionUtil {
	private static final Logger logger = LoggerFactory.getLogger(IbOptionUtil.class);

	public static Optional<LocalDate> convertExpiration(String date) {
		logger.info("Converting String: '{}' to LocalDate", date);

		LocalDate expiration = null;

		if (date.length() == 8) {
			int year = Integer.parseInt(date.substring(0, 4));
			int month = Integer.parseInt(date.substring(4, 6));
			int day = Integer.parseInt(date.substring(6));
			expiration = LocalDate.of(year, month, day);
		} else {
			logger.warn("Incompatible date length, expected 8 characters in: {}", date);
		}

		return Optional.ofNullable(expiration);
	}

}
