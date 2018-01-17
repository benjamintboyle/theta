package theta.portfolio.manager;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Completable;
import theta.domain.Option;
import theta.domain.Theta;
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

public class PositionLogger {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
      Option o1 = (Option) s1;
      Option o2 = (Option) s2;

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
    } else if (s1.getSecurityType() == SecurityType.PUT && s2.getSecurityType() == SecurityType.CALL) {
      return 1;
    } else {
      return 0;
    }
  };

  public static Completable logPositions(Collection<Theta> thetas, Collection<UUID> matchedPositionIds,
      Collection<Security> allSecurities) {

    // Log positions asynchronously
    Completable positionLogger = Completable.create(emitter -> {
      logThetaPositions(thetas);

      logUnmatchedPositions(matchedPositionIds, allSecurities);

      emitter.onComplete();
    });

    return positionLogger;
  }

  public static void logThetaPositions(Collection<Theta> thetas) {
    for (final Theta position : thetas.stream().sorted(Comparator.comparing(Theta::getTicker))
        .collect(Collectors.toList())) {
      logger.info("Current position: {}", position);
    }
  }

  public static void logUnmatchedPositions(Collection<UUID> matchedPositionIds, Collection<Security> allSecurities) {
    for (final Security security : allSecurities.stream()
        .filter(security -> !matchedPositionIds.contains(security.getId()))
        .sorted(byTicker.thenComparing(byStockIsGreaterThanOptions).thenComparing(byOptionExpiration)
            .thenComparing(byPrice).thenComparing(byCallIsGreaterThanPut))
        .collect(Collectors.toList())) {
      logger.info("Current unprocessed security: {}", security);
    }
  }
}
