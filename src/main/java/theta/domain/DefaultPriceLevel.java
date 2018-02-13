package theta.domain;

import java.util.Objects;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;

public class DefaultPriceLevel implements PriceLevel {

  private final Ticker ticker;
  private final Double price;
  private final PriceLevelDirection tradeIf;

  private DefaultPriceLevel(Ticker ticker, Double price, PriceLevelDirection tradeIf) {
    this.ticker = Objects.requireNonNull(ticker, "DefaultPriceLevel expects non-null ticker: " + ticker);
    this.price = Objects.requireNonNull(price, "DefaultPriceLevel expects non-null price: " + price);
    this.tradeIf =
        Objects.requireNonNull(tradeIf, "DefaultPriceLevel expects non-null price level direction:" + tradeIf);
  }

  public static PriceLevel of(Theta theta) {
    return new DefaultPriceLevel(theta.getTicker(), theta.getPrice(), calculateTradeIf(theta));
  }

  public static PriceLevel from(Ticker ticker, Double price, PriceLevelDirection tradeIf) {
    return new DefaultPriceLevel(ticker, price, tradeIf);
  }

  @Override
  public Ticker getTicker() {
    return ticker;
  }

  @Override
  public Double getPrice() {
    return price;
  }

  @Override
  public PriceLevelDirection tradeIf() {
    return tradeIf;
  }

  @Override
  public int compareTo(PriceLevel other) {

    int compareValue = 0;

    if (!getTicker().equals(other.getTicker())) {
      compareValue = getTicker().compareTo(other.getTicker());
    } else if (!getPrice().equals(other.getPrice())) {
      compareValue = getPrice().compareTo(other.getPrice());
    } else {
      compareValue = tradeIf().toString().compareTo(other.tradeIf().toString());
    }

    return compareValue;
  }

  @Override
  public int hashCode() {

    return Objects.hash(getTicker(), getPrice(), tradeIf());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (obj instanceof PriceLevel) {

      final PriceLevel other = (PriceLevel) obj;

      return Objects.equals(getTicker(), other.getTicker()) && Objects.equals(getPrice(), other.getPrice())
          && Objects.equals(tradeIf(), other.tradeIf());

    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Price Level [ Ticker: ");
    builder.append(getTicker());

    builder.append(", Price: ");
    builder.append(getPrice());

    builder.append(", Price Direction: ");
    builder.append(tradeIf());

    builder.append(" ]");

    return builder.toString();
  }

  private static PriceLevelDirection calculateTradeIf(Theta theta) {
    PriceLevelDirection priceLevelDirection = PriceLevelDirection.FALLS_BELOW;

    if (theta.getStock().getQuantity() > 0) {
      priceLevelDirection = PriceLevelDirection.FALLS_BELOW;
    }
    if (theta.getStock().getQuantity() < 0) {
      priceLevelDirection = PriceLevelDirection.RISES_ABOVE;
    }

    return priceLevelDirection;
  }

}
