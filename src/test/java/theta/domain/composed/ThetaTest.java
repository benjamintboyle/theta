package theta.domain.composed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.testutil.ThetaDomainTestUtil;

class ThetaTest {

  @Test
  void testHashCode() throws Exception {

    final Stock stock = ThetaDomainTestUtil.buildTestStock();
    final Option call = ThetaDomainTestUtil.buildCallOption();
    final Option put = ThetaDomainTestUtil.buildPutOption();

    final Theta theta = Theta.of(stock, call, put).get();
    final Theta thetaEqual = Theta.of(stock, call, put).get();

    assertThat(theta, is(not(sameInstance(thetaEqual))));

    assertThat(theta.hashCode(), is(thetaEqual.hashCode()));
  }

  @Test
  void testOfStockOptionOption() {

    final Stock stock = ThetaDomainTestUtil.buildTestStock();
    final Option call = ThetaDomainTestUtil.buildCallOption();
    final Option put = ThetaDomainTestUtil.buildPutOption();

    final Optional<Theta> optionalTheta = Theta.of(stock, call, put);

    assertThat(optionalTheta.isPresent(), is(equalTo(true)));

    final Theta theta = optionalTheta.get();

    assertThat(theta.getStock(), is(equalTo(stock)));
    assertThat(theta.getCall(), is(equalTo(call)));
    assertThat(theta.getPut(), is(equalTo(put)));
  }

  @Test
  void testOfStockShortStraddle() {

    final Stock stock = ThetaDomainTestUtil.buildTestStock();
    final Option call = ThetaDomainTestUtil.buildCallOption();
    final Option put = ThetaDomainTestUtil.buildPutOption();
    final ShortStraddle straddle = ShortStraddle.of(call, put);

    final Optional<Theta> optionalTheta = Theta.of(stock, straddle);

    assertThat(optionalTheta.isPresent(), is(equalTo(true)));

    final Theta theta = optionalTheta.get();

    assertThat(theta.getStock(), is(equalTo(stock)));
    assertThat(theta.getStraddle(), is(equalTo(straddle)));
  }

  @Test
  void testGetQuantity() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    assertThat(theta.getQuantity(), is(equalTo(-1L)));
  }

  @Test
  void testGetSecurityType() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    assertThat(theta.getSecurityType(), is(equalTo(SecurityType.THETA)));
  }

  @Test
  void testGetSecurityOfTypeStock() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    final Security security = theta.getSecurityOfType(SecurityType.STOCK);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.STOCK)));
    assertThat(security, is(equalTo(ThetaDomainTestUtil.buildTestStock())));
  }

  @Test
  void testGetSecurityOfTypeCall() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    final Security security = theta.getSecurityOfType(SecurityType.CALL);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.CALL)));
    assertThat(security, is(equalTo(ThetaDomainTestUtil.buildCallOption())));
  }

  @Test
  void testGetSecurityOfTypePut() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    final Security security = theta.getSecurityOfType(SecurityType.PUT);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.PUT)));
    assertThat(security, is(equalTo(ThetaDomainTestUtil.buildPutOption())));
  }

  @Test
  void testGetSecurityOfTypeException() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    assertThrows(IllegalArgumentException.class, () -> theta.getSecurityOfType(SecurityType.THETA));
  }

  @Test
  void testGetTicker() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    assertThat(theta.getTicker().getSymbol(), is(equalTo("ABC")));
  }

  @Test
  void testToString() throws Exception {

    final Theta theta = ThetaDomainTestUtil.buildTestTheta();

    final String toStringTheta = theta.toString();

    assertThat("toString() should not be empty.", toStringTheta, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringTheta,
        not(containsString("@")));
  }

  @Test
  void testEqualsObject() {
    final Stock stock = ThetaDomainTestUtil.buildTestStock();
    final Option call = ThetaDomainTestUtil.buildCallOption();
    final Option put = ThetaDomainTestUtil.buildPutOption();
    final ShortStraddle straddle = ShortStraddle.of(call, put);

    final Optional<Theta> optionalThetaStraddle = Theta.of(stock, straddle);
    final Optional<Theta> optionalThetaOption = Theta.of(stock, call, put);

    assertThat(optionalThetaStraddle.isPresent(), is(equalTo(true)));
    assertThat(optionalThetaOption.isPresent(), is(equalTo(true)));

    final Theta thetaStraddle = optionalThetaStraddle.get();
    final Theta thetaOption = optionalThetaOption.get();

    assertThat(thetaStraddle, is(equalTo(thetaOption)));
  }

}
