package theta.domain.util;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.option.Option;
import theta.domain.stock.Stock;

public class SecurityUtil {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private SecurityUtil() {}

  public static Optional<Security> getSecurityWithQuantity(Security security, long adjustment) {

    Optional<Security> securityWithQuantity = Optional.empty();

    if (adjustment < 0) {
      logger.warn("Adjustment was negative: {}. Always assumed to be positive. Setting to: {} for Security: {}",
          adjustment, Math.abs(adjustment), security);
      adjustment = Math.abs(adjustment);
    }

    // If unallocated and security are equal, just return security
    if (adjustment == Math.abs(security.getQuantity())) {
      securityWithQuantity = Optional.of(security);
    }

    // If there are no unallocated then do nothing
    else if (adjustment == 0) {
      logger.debug("All securities have been allocated for {}", security);
    }

    // Main condition where security is subdivided
    else if (adjustment < Math.abs(security.getQuantity())) {

      logger.debug("Subdividing security quantity to {} for {}", adjustment, security);

      switch (security.getSecurityType()) {
        case STOCK:
          securityWithQuantity = Optional.of(Stock.of(security.getId(), security.getTicker(),
              adjustment * Long.signum(security.getQuantity()), security.getPrice()));
          break;
        case CALL:
        case PUT:
          if (security instanceof Option) {
            final Option inputOption = (Option) security;

            securityWithQuantity = Optional.of(new Option(inputOption.getId(), inputOption.getSecurityType(),
                inputOption.getTicker(), adjustment * Long.signum(security.getQuantity()), inputOption.getStrikePrice(),
                inputOption.getExpiration(), inputOption.getAverageTradePrice()));
          } else {
            logger.warn("Tried to adjust a security is not an Option object: {}", security);
          }
          break;
        default:
          logger.warn("Invalid security type {} for reducing quantity {}", security.getSecurityType(), security);
      }
    }

    // Error case where unallocated are greater than security (should never happen)
    else {
      logger.warn("Skipping security. Calculated inaccurate quantity of {} unallocated securities (too many) for {}",
          adjustment, security);
    }

    return securityWithQuantity;
  }

}
