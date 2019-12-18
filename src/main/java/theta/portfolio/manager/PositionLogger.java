package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.composed.Theta;
import theta.domain.option.Option;

public class PositionLogger {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static Comparator<Security> byTicker = Comparator.comparing(Security::getTicker);

  private static Comparator<Security> byStockIsGreaterThanOptions = (s1, s2) -> {
    if (s1.getSecurityType() == SecurityType.STOCK) {
      return -1;
    } else if (s2.getSecurityType() == SecurityType.STOCK) {
      return 1;
    } else {
      return 0;
    }
  };

  private static Comparator<Security> byOptionExpiration = (s1, s2) -> {
    if (s1 instanceof Option && s2 instanceof Option) {
      final Option o1 = (Option) s1;
      final Option o2 = (Option) s2;

      if (o1.getExpiration().isAfter(o2.getExpiration())) {
        return 1;
      } else if (o2.getExpiration().isAfter(o1.getExpiration())) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  };

  private static Comparator<Security> byPrice = Comparator.comparing(Security::getPrice);

  private static Comparator<Security> byCallIsGreaterThanPut = (s1, s2) -> {
    if (s1.getSecurityType() == SecurityType.CALL && s2.getSecurityType() == SecurityType.PUT) {
      return -1;
    } else if (s1.getSecurityType() == SecurityType.PUT
        && s2.getSecurityType() == SecurityType.CALL) {
      return 1;
    } else {
      return 0;
    }
  };

  private PositionLogger() {

  }

  /**
   * Helper to log all positions and status.
   *
   * @param thetaIdMap All ThetaTrades mapped to their Id.
   * @param securityThetaLink All Securities in ThetaTrades mapped to their Id.
   * @param securityIdMap All Securities (including unmapped) mapped to their id.
   */
  public static void logPositions(Map<UUID, Theta> thetaIdMap,
      Map<UUID, Set<UUID>> securityThetaLink, Map<UUID, Security> securityIdMap) {

    logger.info("Position Logging Start");

    logThetaPositions(thetaIdMap.values());

    logUnmatchedPositions(securityThetaLink.keySet(), securityIdMap.values());

    logAllSecurities(securityIdMap.values());

    logger.info("Position Logging Complete");
  }

  private static void logThetaPositions(Collection<Theta> thetas) {

    final List<Theta> thetasSorted =
        thetas.stream().sorted(Comparator.comparing(Theta::getTicker)).collect(Collectors.toList());

    for (final Theta position : thetasSorted) {
      logger.info("Current position: {}", position);
    }

    if (thetasSorted.isEmpty()) {
      logger.info("No Thetas");
    }
  }

  private static void logUnmatchedPositions(Collection<UUID> matchedPositionIds,
      Collection<Security> allSecurities) {

    final List<Security> unmatched =
        allSecurities.stream().filter(security -> !matchedPositionIds.contains(security.getId()))
            .sorted(byTicker.thenComparing(byStockIsGreaterThanOptions)
                .thenComparing(byOptionExpiration).thenComparing(byPrice)
                .thenComparing(byCallIsGreaterThanPut))
            .collect(Collectors.toList());

    for (final Security security : unmatched) {
      logger.warn("Unmatched security: {}", security);
    }

    if (unmatched.isEmpty()) {
      logger.info("No Unmatched Positions");
    }
  }

  private static void logAllSecurities(Collection<Security> allSecurities) {
    for (final Security security : allSecurities.stream()
        .sorted(
            byTicker.thenComparing(byStockIsGreaterThanOptions).thenComparing(byOptionExpiration)
                .thenComparing(byPrice).thenComparing(byCallIsGreaterThanPut))
        .collect(Collectors.toList())) {
      logger.debug("List of all securities: {}", security);
    }
  }

}
