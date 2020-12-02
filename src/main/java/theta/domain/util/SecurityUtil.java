package theta.domain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.option.Option;
import theta.domain.stock.Stock;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class SecurityUtil {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SecurityUtil() {
    }

    /**
     * Get Securities, if available.
     *
     * @param security   Available securities.
     * @param adjustment Number of securities requested.
     * @return Optional if number of Securities they are provided.
     */
    public static Optional<Security> getSecurityWithQuantity(Security security, long adjustment) {
        Optional<Security> securityWithQuantity = Optional.empty();

        if (adjustment <= 0) {
            logger.warn("Adjustment was negative: {}. Returning nothing for: {}", adjustment, security);
            return Optional.empty();
        }

        if (adjustment <= Math.abs(security.getQuantity())) {    // If there are no unallocated then do nothing
            logger.debug("Subdividing security quantity to {} for {}", adjustment, security);

            securityWithQuantity = switch (security.getSecurityType()) {
                case STOCK -> Optional.of(Stock.of(security.getId(), security.getTicker(), adjustment * Long.signum(security.getQuantity()), security.getPrice()));
                case CALL, PUT -> {
                    Option inputOption = (Option) security;
                    yield Optional.of(
                            new Option(inputOption.getId(),
                                    inputOption.getSecurityType(),
                                    inputOption.getTicker(),
                                    adjustment * Long.signum(security.getQuantity()),
                                    inputOption.getStrikePrice(),
                                    inputOption.getExpiration(),
                                    inputOption.getAverageTradePrice()));
                }
                case THETA, SHORT_STRADDLE -> {
                    logger.warn("Tried to adjust a security is not a supported type: {}", security);
                    yield Optional.empty();
                }
            };
        } else {
            logger.warn("Adjustment of {} was too big for {}", adjustment, security);
        }

        return securityWithQuantity;
    }
}
