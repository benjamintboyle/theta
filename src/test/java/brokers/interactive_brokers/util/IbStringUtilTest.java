package brokers.interactive_brokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;
import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.contracts.StkContract;
import theta.execution.api.ExecutableOrder;
import theta.execution.domain.ExecutionDomainBuilderUtil;

class IbStringUtilTest {

  @Test
  void testToStringOrderState() throws NoSuchMethodException, SecurityException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    // Check that all OrderState constructors are NOT public. This test exists so that if a public
    // constructor is exposed in a future version of the API, this test will fail and all the below
    // reflection for OrderState can be removed in favor of the public constructor.
    assertThat("Test for OrderState toString should use available 'public' constructor.",
        OrderState.class.getConstructors().length, is(0));

    // Reflection needed as all OrderState constructors have default access modifier
    Constructor<OrderState> constructor = OrderState.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    OrderState orderState = constructor.newInstance();

    orderState.commission(1.0);
    orderState.commissionCurrency("Dollars");
    orderState.equityWithLoan("equityWithLoan");
    orderState.initMargin("initialMargin");
    orderState.maintMargin("maintenanceMargin");
    orderState.maxCommission(1000.0);
    orderState.minCommission(0.0);
    orderState.status(OrderStatus.Filled);
    orderState.warningText("warningText");

    String toStringOrderState = IbStringUtil.toStringOrderState(orderState);

    assertThat("toString() should not be empty.", toStringOrderState, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringOrderState,
        not(containsString("@")));
  }

  @Test
  void testToStringOrderStatus() {

    String toStringOrderStatus =
        IbStringUtil.toStringOrderStatus(OrderStatus.Submitted, 100.1, 32.1, 123.2, 9L, 23, 32.9, 0, "Reason Held");

    assertThat("toString() should not be empty.", toStringOrderStatus, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringOrderStatus,
        not(containsString("@")));
  }

  @Test
  void testToStringOrder() {

    ExecutableOrder executableOrder = ExecutionDomainBuilderUtil.buildTestDefaultStockOrderNewBuyLimit();
    Order order = IbOrderUtil.buildIbOrder(executableOrder);

    String toStringOrder = IbStringUtil.toStringOrder(order);

    assertThat("toString() should not be empty.", toStringOrder, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringOrder, not(containsString("@")));
  }

  @Test
  void testToStringContract() {
    Contract contract = new StkContract("ABC");

    String toStringContract = IbStringUtil.toStringContract(contract);

    assertThat("toString() should not be empty.", toStringContract, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringContract, not(containsString("@")));
  }

  @Test
  void testToStringContractNull() {

    String toStringContractNull = IbStringUtil.toStringContract(null);

    assertThat(toStringContractNull, is("null"));
  }

  @Test
  void testToStringDeltaNeutralContractEmpty() {
    String toStringDeltaNeutralContractEmpty = IbStringUtil.toStringDeltaNeutralContract(new DeltaNeutralContract());

    assertThat("toString() should not be empty.", toStringDeltaNeutralContractEmpty, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringDeltaNeutralContractEmpty,
        not(containsString("@")));
  }

  @Test
  void testToStringDeltaNeutralContractNull() {
    String toStringDeltaNeutralNull = IbStringUtil.toStringDeltaNeutralContract(null);

    assertThat(toStringDeltaNeutralNull, is("null"));
  }

  @Test
  void testToStringComboLegEmpty() {
    String toStringComboLegEmpty = IbStringUtil.toStringComboLeg(new ComboLeg());

    assertThat("toString() should not be empty.", toStringComboLegEmpty, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringComboLegEmpty,
        not(containsString("@")));
  }

  @Test
  void testToStringComboLegNull() {
    String toStringComboLegNull = IbStringUtil.toStringComboLeg(null);

    assertThat(toStringComboLegNull, is("null"));
  }

}
