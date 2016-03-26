package theta.portfolio.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.domain.Option;
import theta.domain.Stock;
import theta.domain.ThetaTrade;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class UnprocessedPositionManager {
	private static final Logger logger = LoggerFactory.getLogger(UnprocessedPositionManager.class);

	private PortfolioManager portfolioManager;
	private List<Security> unprocessedSecurities = new ArrayList<Security>();

	public UnprocessedPositionManager(PortfolioManager portfolioManager) {
		this.portfolioManager = portfolioManager;
	}

	public void processSecurity(Security security) {
		logger.info("Adding unprocessed security: {}", security);
		this.unprocessedSecurities.add(security);

		this.logUnprocessedList();

		Optional<ThetaTrade> theta = Optional.empty();

		// Filter by ticker, then group by security type
		Map<SecurityType, List<Security>> bySecurityType = this.unprocessedSecurities.stream()
				.filter(unprocessed -> unprocessed.getTicker().equals(security.getTicker()))
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

		if (theta.isPresent()) {
			this.portfolioManager.processPosition(theta.get());
		}
	}

	public void processSecurities(Security security, List<Security> reprocessedSecurities) {
		List<Security> securitiesToDiscard = this.unprocessedSecurities.stream()
				.filter(discard -> discard.getTicker().equals(security.getTicker()))
				.filter(discard -> discard.getSecurityType().equals(security.getSecurityType()))
				.collect(Collectors.toList());

		if (SecurityType.CALL.equals(security.getSecurityType())
				|| SecurityType.PUT.equals(security.getSecurityType())) {
			securitiesToDiscard = securitiesToDiscard.stream()
					.filter(discard -> discard.getPrice() == security.getPrice()).collect(Collectors.toList());
		}

		this.unprocessedSecurities.removeAll(securitiesToDiscard);

		this.processSecurities(reprocessedSecurities);
		this.processSecurity(security);
	}

	public void processSecurities(List<Security> securities) {
		for (Security security : securities) {
			this.processSecurity(security);
		}
	}

	private void logUnprocessedList() {
		if (this.unprocessedSecurities.size() > 0) {
			logger.info("Logging Unprocessed List");
			for (Security security : this.unprocessedSecurities) {
				logger.info("Unprocessed Security: {}", security);
			}
			logger.info("Completed Logging Unprocessed List");
		} else {
			logger.info("Unprocessed List is empty");
		}
	}

	public List<Security> getUnprocessedSecurities(String ticker) {
		return this.unprocessedSecurities.stream().filter(security -> security.getTicker().equals(ticker))
				.collect(Collectors.toList());
	}
}
