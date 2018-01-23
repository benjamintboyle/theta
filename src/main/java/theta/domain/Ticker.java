package theta.domain;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Ticker implements Comparable<Ticker> {

  private static ConcurrentMap<String, Ticker> mapOfTickers = new ConcurrentHashMap<>();

  private final String ticker;

  private Ticker(String ticker) {
    this.ticker = ticker;
  }

  public static Ticker from(String ticker) {

    return mapOfTickers.computeIfAbsent(ticker, Ticker::new);
  }

  @Override
  public String toString() {
    return ticker;
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.ticker);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    final Ticker other = (Ticker) obj;

    return Objects.equals(this.ticker, other.ticker);
  }

  @Override
  public int compareTo(Ticker other) {

    return this.ticker.compareTo(other.ticker);
  }
}
