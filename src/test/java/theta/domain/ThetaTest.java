package theta.domain;

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
import theta.domain.api.Security;
import theta.domain.api.SecurityType;

class ThetaTest {

  @Test
  void testHashCode() throws Exception {

    Stock stock = ThetaDomainFactory.buildTestStock();
    Option call = ThetaDomainFactory.buildCallOption();
    Option put = ThetaDomainFactory.buildPutOption();

    Theta theta = Theta.of(stock, call, put).get();
    Theta thetaEqual = Theta.of(stock, call, put).get();

    assertThat(theta, is(not(sameInstance(thetaEqual))));

    assertThat(theta.hashCode(), is(thetaEqual.hashCode()));
  }

  @Test
  void testOfStockOptionOption() {

    Stock stock = ThetaDomainFactory.buildTestStock();
    Option call = ThetaDomainFactory.buildCallOption();
    Option put = ThetaDomainFactory.buildPutOption();

    Optional<Theta> optionalTheta = Theta.of(stock, call, put);

    assertThat(optionalTheta.isPresent(), is(equalTo(true)));

    Theta theta = optionalTheta.get();

    assertThat(theta.getStock(), is(equalTo(stock)));
    assertThat(theta.getCall(), is(equalTo(call)));
    assertThat(theta.getPut(), is(equalTo(put)));
  }

  @Test
  void testOfStockShortStraddle() {

    Stock stock = ThetaDomainFactory.buildTestStock();
    Option call = ThetaDomainFactory.buildCallOption();
    Option put = ThetaDomainFactory.buildPutOption();
    ShortStraddle straddle = ShortStraddle.of(call, put);

    Optional<Theta> optionalTheta = Theta.of(stock, straddle);

    assertThat(optionalTheta.isPresent(), is(equalTo(true)));

    Theta theta = optionalTheta.get();

    assertThat(theta.getStock(), is(equalTo(stock)));
    assertThat(theta.getStraddle(), is(equalTo(straddle)));
  }

  @Test
  void testGetQuantity() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    assertThat(theta.getQuantity(), is(equalTo(-1L)));
  }

  @Test
  void testGetSecurityType() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    assertThat(theta.getSecurityType(), is(equalTo(SecurityType.THETA)));
  }

  @Test
  void testGetSecurityOfTypeStock() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    Security security = theta.getSecurityOfType(SecurityType.STOCK);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.STOCK)));
    assertThat(security, is(equalTo(ThetaDomainFactory.buildTestStock())));
  }

  @Test
  void testGetSecurityOfTypeCall() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    Security security = theta.getSecurityOfType(SecurityType.CALL);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.CALL)));
    assertThat(security, is(equalTo(ThetaDomainFactory.buildCallOption())));
  }

  @Test
  void testGetSecurityOfTypePut() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    Security security = theta.getSecurityOfType(SecurityType.PUT);

    assertThat(security.getSecurityType(), is(equalTo(SecurityType.PUT)));
    assertThat(security, is(equalTo(ThetaDomainFactory.buildPutOption())));
  }

  @Test
  void testGetSecurityOfTypeException() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    assertThrows(IllegalArgumentException.class, () -> theta.getSecurityOfType(SecurityType.THETA));
  }

  @Test
  void testGetTicker() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    assertThat(theta.getTicker().getSymbol(), is(equalTo("ABC")));
  }

  @Test
  void testToString() throws Exception {

    Theta theta = ThetaDomainFactory.buildTestTheta();

    String toStringTheta = theta.toString();

    assertThat("toString() should not be empty.", toStringTheta, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringTheta, not(containsString("@")));
  }

  @Test
  void testEqualsObject() {
    Stock stock = ThetaDomainFactory.buildTestStock();
    Option call = ThetaDomainFactory.buildCallOption();
    Option put = ThetaDomainFactory.buildPutOption();
    ShortStraddle straddle = ShortStraddle.of(call, put);

    Optional<Theta> optionalThetaStraddle = Theta.of(stock, straddle);
    Optional<Theta> optionalThetaOption = Theta.of(stock, call, put);

    assertThat(optionalThetaStraddle.isPresent(), is(equalTo(true)));
    assertThat(optionalThetaOption.isPresent(), is(equalTo(true)));

    Theta thetaStraddle = optionalThetaStraddle.get();
    Theta thetaOption = optionalThetaOption.get();

    assertThat(thetaStraddle, is(equalTo(thetaOption)));
  }

}
