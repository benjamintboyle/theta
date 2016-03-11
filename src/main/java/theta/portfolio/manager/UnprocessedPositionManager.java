package theta.portfolio.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.api.Security;
import theta.api.SecurityType;
import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;

public class UnprocessedPositionManager {
	private final Logger logger = LoggerFactory.getLogger(UnprocessedPositionManager.class);

	private List<Security> unprocessedSecurities = new ArrayList<Security>();

	public Optional<ThetaTrade> add(Security security) {
		logger.info("Adding unprocessed security: {}", security);
		this.unprocessedSecurities.add(security);
		this.logUnprocessedList();
		return this.processSecurities(security.getTicker());
	}

	private Optional<ThetaTrade> processSecurities(String ticker) {
		Optional<ThetaTrade> theta = Optional.empty();

		// Filter by ticker, then group by security type
		Map<SecurityType, List<Security>> bySecurityType = this.unprocessedSecurities.stream()
				.filter(unprocessed -> unprocessed.getTicker().equals(ticker))
				.collect(Collectors.groupingBy(Security::getSecurityType));
		// if there are 3 security types (Stock, Call, Put)
		if (bySecurityType.size() == 3) {
			for (Security call : bySecurityType.get(SecurityType.CALL)) {
				List<Security> putsAtStrike = bySecurityType.get(SecurityType.PUT).stream()
						.filter(put -> put.getPrice().equals(call.getPrice())).collect(Collectors.toList());

				Stock stock = (Stock) bySecurityType.get(SecurityType.STOCK).get(0);
				Option callOption = (Option) call;
				Option putOption = (Option) putsAtStrike.get(0);

				theta = ThetaTrade.of(stock, callOption, putOption);
				logger.info("Created Theta from unprocessed securities: {}", theta);

				this.unprocessedSecurities.remove(stock);
				this.unprocessedSecurities.remove(callOption);
				this.unprocessedSecurities.remove(putOption);

				this.logUnprocessedList();
			}
		} else {
			logger.info("Not enough securities to form trade: {}", bySecurityType);
		}

		return theta;
	}

	private void logUnprocessedList() {
		logger.info("Logging Unprocessed List");
		for (Security security : this.unprocessedSecurities) {
			logger.info("Unprocessed Security: {}", security);
		}
		logger.info("Completed Logging Unprocessed List");
	}
}
