package theta.domain;

public class ThetaDomainFactory {

  public static Stock buildTestStock() {
    return Stock.of(Ticker.from("ABC"), -100L, 123.45);
  }
}
