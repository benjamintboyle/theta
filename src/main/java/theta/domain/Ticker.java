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

  public String getSymbol() {
    return tickerSymbol;
  }

  @Override
  public String toString() {
    return getSymbol();
  }

  @Override
  public int hashCode() {

    return Objects.hash(getSymbol());
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

    return Objects.equals(getSymbol(), other.getSymbol());
  }

  @Override
  public int compareTo(Ticker other) {

    return getSymbol().compareTo(other.getSymbol());
  }
}
