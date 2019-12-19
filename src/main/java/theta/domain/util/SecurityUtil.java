package theta.domain.util;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import theta.domain.Security;
import theta.domain.option.Option;
import theta.domain.stock.Stock;

@Slf4j
public class SecurityUtil {

  private SecurityUtil() {

  }

  /**
   * Get Securities, if available.
   *
   * @param security Available securities.
   * @param adjustment Number of securities requested.
   * @return Optional if number of Securities they are provided.
   */
  public static Optional<Security> getSecurityWithQuantity(Security security, long adjustment) {

    Optional<Security> securityWithQuantity = Optional.empty();

    if (adjustment < 0) {
      log.warn("Adjustment was negative: {}. Always assumed to be positive. Setting to: {} "
          + "for Security: {}", adjustment, Math.abs(adjustment), security);
      adjustment = Math.abs(adjustment);
    }

    // If unallocated and security are equal, just return security
    if (adjustment == Math.abs(security.getQuantity())) {
      securityWithQuantity = Optional.of(security);
      // If there are no unallocated then do nothing
    } else if (adjustment == 0) {
      log.debug("All securities have been allocated for {}", security);
      // Main condition where security is subdivided
    } else if (adjustment < Math.abs(security.getQuantity())) {

      log.debug("Subdividing security quantity to {} for {}", adjustment, security);

      switch (security.getSecurityType()) {
        case STOCK:
          securityWithQuantity = Optional.of(Stock.of(security.getId(), security.getTicker(),
              adjustment * Long.signum(security.getQuantity()), security.getPrice()));
          break;
        case CALL:
        case PUT:
          if (security instanceof Option) {
            final Option inputOption = (Option) security;

            securityWithQuantity = Optional.of(new Option(inputOption.getId(),
                inputOption.getSecurityType(), inputOption.getTicker(),
                adjustment * Long.signum(security.getQuantity()), inputOption.getStrikePrice(),
                inputOption.getExpiration(), inputOption.getAverageTradePrice()));
          } else {
            log.warn("Tried to adjust a security is not an Option object: {}", security);
          }
          break;
        default:
          log.warn("Invalid security type {} for reducing quantity {}", security.getSecurityType(),
              security);
      }
      // Error case where unallocated are greater than security (should never happen)
    } else {
      log.warn("Skipping security. Calculated inaccurate quantity of {} unallocated securities "
          + "(too many) for {}", adjustment, security);
    }

    return securityWithQuantity;
  }

}
