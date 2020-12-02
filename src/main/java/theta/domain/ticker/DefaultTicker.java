package theta.domain.ticker;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import theta.domain.Ticker;

public class DefaultTicker implements Ticker {

  private static final ConcurrentMap<String, Ticker> mapOfTickers = new ConcurrentHashMap<>();

  private final String tickerSymbol;

  private DefaultTicker(String ticker) {
    tickerSymbol = ticker;
  }

  public static Ticker from(String ticker) {

    return mapOfTickers.computeIfAbsent(ticker, DefaultTicker::new);
  }

  /*
   * (non-Javadoc)
   *
   * @see theta.domain.Ticker#getSymbol()
   */
  @Override
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

    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Ticker other = (Ticker) obj;

    return Objects.equals(getSymbol(), other.getSymbol());
  }

  @Override
  public int compareTo(Ticker other) {

    return getSymbol().compareTo(other.getSymbol());
  }
}
