package theta.domain.api;

public enum SecurityType {
  // Note, STOCK, CALL, PUT indicate naked stock/option positions
  CALL, PUT, SHORT_STRADDLE, STOCK, THETA
}
