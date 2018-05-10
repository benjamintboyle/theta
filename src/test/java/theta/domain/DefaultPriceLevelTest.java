package theta.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;

class DefaultPriceLevelTest {

  @Test
  void testHashCode() {

    PriceLevel defaultPriceLevel = ThetaDomainFactory.buildDefaultPriceLevel();
    PriceLevel defaultPriceLevelEqual = ThetaDomainFactory.buildDefaultPriceLevel();
    PriceLevel defaultPriceLevelNotEqualTicker =
        DefaultPriceLevel.from(Ticker.from("XYZ"), defaultPriceLevel.getPrice(), defaultPriceLevel.tradeIf());
    PriceLevel defaultPriceLevelNotEqualPrice =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), 1.0, defaultPriceLevel.tradeIf());
    PriceLevel defaultPriceLevelNotEqualDirection = DefaultPriceLevel.from(defaultPriceLevel.getTicker(),
        defaultPriceLevel.getPrice(), PriceLevelDirection.FALLS_BELOW);

    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelEqual))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualTicker))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualPrice))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualDirection))));

    assertThat(defaultPriceLevel.hashCode(), is(equalTo(defaultPriceLevelEqual.hashCode())));
    assertThat(defaultPriceLevel.hashCode(), is(not(equalTo(defaultPriceLevelNotEqualTicker.hashCode()))));
    assertThat(defaultPriceLevel.hashCode(), is(not(equalTo(defaultPriceLevelNotEqualPrice.hashCode()))));
    assertThat(defaultPriceLevel.hashCode(), is(not(equalTo(defaultPriceLevelNotEqualDirection.hashCode()))));
  }

  @Test
  void testOf() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    PriceLevel defaultPriceLevel = DefaultPriceLevel.of(theta);

    assertThat(defaultPriceLevel.getTicker().getSymbol(), is(equalTo("ABC")));
    assertThat(defaultPriceLevel.getPrice(), is(equalTo(123.5)));
    assertThat(defaultPriceLevel.tradeIf(), is(equalTo(PriceLevelDirection.RISES_ABOVE)));
  }

  @Test
  void testFrom() {
    PriceLevel defaultPriceLevel = DefaultPriceLevel.from(Ticker.from("XYZ"), 1.0, PriceLevelDirection.FALLS_BELOW);

    assertThat(defaultPriceLevel.getTicker().getSymbol(), is(equalTo("XYZ")));
    assertThat(defaultPriceLevel.getPrice(), is(equalTo(1.0)));
    assertThat(defaultPriceLevel.tradeIf(), is(equalTo(PriceLevelDirection.FALLS_BELOW)));
  }

  @Test
  void testCompareTo() {

    PriceLevel priceLevelFirst = DefaultPriceLevel.from(Ticker.from("XYZ"), 1.0, PriceLevelDirection.FALLS_BELOW);
    PriceLevel priceLevelSecond = DefaultPriceLevel.from(Ticker.from("XYZ"), 1.0, PriceLevelDirection.RISES_ABOVE);
    PriceLevel priceLevelThird = DefaultPriceLevel.from(Ticker.from("XYZ"), 0.5, PriceLevelDirection.FALLS_BELOW);
    PriceLevel priceLevelFourth = DefaultPriceLevel.from(Ticker.from("AAA"), 1.0, PriceLevelDirection.FALLS_BELOW);

    List<PriceLevel> sortedList =
        Arrays.asList(priceLevelFirst, priceLevelSecond, priceLevelThird, priceLevelFourth).stream().sorted().collect(
            Collectors.toList());

    List<PriceLevel> expectedList = Arrays.asList(priceLevelFourth, priceLevelThird, priceLevelFirst, priceLevelSecond);
    List<PriceLevel> wrongOrderedList =
        Arrays.asList(priceLevelFirst, priceLevelSecond, priceLevelThird, priceLevelFourth);

    assertThat(sortedList, is(equalTo(expectedList)));
    assertThat(sortedList, is(not(equalTo(wrongOrderedList))));
  }

  @Test
  void testEquals() {

    PriceLevel defaultPriceLevel = ThetaDomainFactory.buildDefaultPriceLevel();
    PriceLevel defaultPriceLevelEqual = ThetaDomainFactory.buildDefaultPriceLevel();
    PriceLevel defaultPriceLevelNotEqualTicker =
        DefaultPriceLevel.from(Ticker.from("XYZ"), defaultPriceLevel.getPrice(), defaultPriceLevel.tradeIf());
    PriceLevel defaultPriceLevelNotEqualPrice =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), 1.0, defaultPriceLevel.tradeIf());
    PriceLevel defaultPriceLevelNotEqualDirection = DefaultPriceLevel.from(defaultPriceLevel.getTicker(),
        defaultPriceLevel.getPrice(), PriceLevelDirection.FALLS_BELOW);

    assertThat(defaultPriceLevel, not(sameInstance(defaultPriceLevelEqual)));
    assertThat(defaultPriceLevel, not(sameInstance(defaultPriceLevelNotEqualTicker)));
    assertThat(defaultPriceLevel, not(sameInstance(defaultPriceLevelNotEqualPrice)));
    assertThat(defaultPriceLevel, not(sameInstance(defaultPriceLevelNotEqualDirection)));

    assertThat(defaultPriceLevel, is(equalTo(defaultPriceLevelEqual)));
    assertThat(defaultPriceLevel, is(not(equalTo(defaultPriceLevelNotEqualTicker))));
    assertThat(defaultPriceLevel, is(not(equalTo(defaultPriceLevelNotEqualPrice))));
    assertThat(defaultPriceLevel, is(not(equalTo(defaultPriceLevelNotEqualDirection))));
  }

  @Test
  void testToString() {

    PriceLevel priceLevel = ThetaDomainFactory.buildDefaultPriceLevel();

    String toStringPriceLevel = priceLevel.toString();

    assertThat("toString() should not be empty.", toStringPriceLevel, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringPriceLevel,
        not(containsString("@")));
  }

}
