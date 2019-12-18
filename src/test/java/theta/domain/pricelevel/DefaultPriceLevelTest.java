package theta.domain.pricelevel;

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
import theta.domain.PriceLevel;
import theta.domain.PriceLevelDirection;
import theta.domain.composed.Theta;
import theta.domain.testutil.ThetaDomainTestUtil;
import theta.domain.ticker.DefaultTicker;

class DefaultPriceLevelTest {

  @Test
  void testHashCode() {

    final PriceLevel defaultPriceLevel = ThetaDomainTestUtil.buildDefaultPriceLevel();
    final PriceLevel defaultPriceLevelEqual = ThetaDomainTestUtil.buildDefaultPriceLevel();
    final PriceLevel defaultPriceLevelNotEqualTicker = DefaultPriceLevel
        .from(DefaultTicker.from("XYZ"), defaultPriceLevel.getPrice(), defaultPriceLevel.tradeIf());
    final PriceLevel defaultPriceLevelNotEqualPrice =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), 1.0, defaultPriceLevel.tradeIf());
    final PriceLevel defaultPriceLevelNotEqualDirection =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), defaultPriceLevel.getPrice(),
            PriceLevelDirection.FALLS_BELOW);

    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelEqual))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualTicker))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualPrice))));
    assertThat(defaultPriceLevel, is(not(sameInstance(defaultPriceLevelNotEqualDirection))));

    assertThat(defaultPriceLevel.hashCode(), is(equalTo(defaultPriceLevelEqual.hashCode())));
    assertThat(defaultPriceLevel.hashCode(),
        is(not(equalTo(defaultPriceLevelNotEqualTicker.hashCode()))));
    assertThat(defaultPriceLevel.hashCode(),
        is(not(equalTo(defaultPriceLevelNotEqualPrice.hashCode()))));
    assertThat(defaultPriceLevel.hashCode(),
        is(not(equalTo(defaultPriceLevelNotEqualDirection.hashCode()))));
  }

  @Test
  void testOf() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    final PriceLevel defaultPriceLevel = DefaultPriceLevel.of(theta);

    assertThat(defaultPriceLevel.getTicker().getSymbol(), is(equalTo("ABC")));
    assertThat(defaultPriceLevel.getPrice(), is(equalTo(123.5)));
    assertThat(defaultPriceLevel.tradeIf(), is(equalTo(PriceLevelDirection.RISES_ABOVE)));
  }

  @Test
  void testFrom() {
    final PriceLevel defaultPriceLevel =
        DefaultPriceLevel.from(DefaultTicker.from("XYZ"), 1.0, PriceLevelDirection.FALLS_BELOW);

    assertThat(defaultPriceLevel.getTicker().getSymbol(), is(equalTo("XYZ")));
    assertThat(defaultPriceLevel.getPrice(), is(equalTo(1.0)));
    assertThat(defaultPriceLevel.tradeIf(), is(equalTo(PriceLevelDirection.FALLS_BELOW)));
  }

  @Test
  void testCompareTo() {

    final PriceLevel priceLevelFirst =
        DefaultPriceLevel.from(DefaultTicker.from("XYZ"), 1.0, PriceLevelDirection.FALLS_BELOW);
    final PriceLevel priceLevelSecond =
        DefaultPriceLevel.from(DefaultTicker.from("XYZ"), 1.0, PriceLevelDirection.RISES_ABOVE);
    final PriceLevel priceLevelThird =
        DefaultPriceLevel.from(DefaultTicker.from("XYZ"), 0.5, PriceLevelDirection.FALLS_BELOW);
    final PriceLevel priceLevelFourth =
        DefaultPriceLevel.from(DefaultTicker.from("AAA"), 1.0, PriceLevelDirection.FALLS_BELOW);

    final List<PriceLevel> sortedList =
        Arrays.asList(priceLevelFirst, priceLevelSecond, priceLevelThird, priceLevelFourth).stream()
            .sorted().collect(Collectors.toList());

    final List<PriceLevel> expectedList =
        Arrays.asList(priceLevelFourth, priceLevelThird, priceLevelFirst, priceLevelSecond);
    final List<PriceLevel> wrongOrderedList =
        Arrays.asList(priceLevelFirst, priceLevelSecond, priceLevelThird, priceLevelFourth);

    assertThat(sortedList, is(equalTo(expectedList)));
    assertThat(sortedList, is(not(equalTo(wrongOrderedList))));
  }

  @Test
  void testEquals() {

    final PriceLevel defaultPriceLevel = ThetaDomainTestUtil.buildDefaultPriceLevel();
    final PriceLevel defaultPriceLevelEqual = ThetaDomainTestUtil.buildDefaultPriceLevel();
    final PriceLevel defaultPriceLevelNotEqualTicker = DefaultPriceLevel
        .from(DefaultTicker.from("XYZ"), defaultPriceLevel.getPrice(), defaultPriceLevel.tradeIf());
    final PriceLevel defaultPriceLevelNotEqualPrice =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), 1.0, defaultPriceLevel.tradeIf());
    final PriceLevel defaultPriceLevelNotEqualDirection =
        DefaultPriceLevel.from(defaultPriceLevel.getTicker(), defaultPriceLevel.getPrice(),
            PriceLevelDirection.FALLS_BELOW);

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

    final PriceLevel priceLevel = ThetaDomainTestUtil.buildDefaultPriceLevel();

    final String toStringPriceLevel = priceLevel.toString();

    assertThat("toString() should not be empty.", toStringPriceLevel, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringPriceLevel,
        not(containsString("@")));
  }

}
