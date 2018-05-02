package theta.domain;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Ticker implements Comparable<Ticker> {

  private static ConcurrentMap<String, Ticker> mapOfTickers = new ConcurrentHashMap<>();

  private final String tickerSymbol;

  private Ticker(String ticker) {
    this.tickerSymbol = ticker;
  }

  public static Ticker from(String ticker) {

    return mapOfTickers.computeIfAbsent(ticker, Ticker::new);
  }

  @Override
  public String toString() {
    return tickerSymbol;
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.tickerSymbol);
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

    return Objects.equals(this.tickerSymbol, other.tickerSymbol);
  }

  @Override
  public int compareTo(Ticker other) {

    return this.tickerSymbol.compareTo(other.tickerSymbol);
  }
}
