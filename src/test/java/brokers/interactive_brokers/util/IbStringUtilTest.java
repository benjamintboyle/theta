package brokers.interactive_brokers.util;

import com.ib.client.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class IbStringUtilTest {

    @Test
    void toStringOrderState() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<OrderState> constructor = OrderState.class.getDeclaredConstructor(
                String.class,  // status
                String.class,  // initMargin
                String.class,  // maintMargin
                String.class,  // equityWithLoan
                double.class,  // commission
                double.class,  // minCommission
                double.class,  // maxCommission
                String.class,  // commissionCurrency
                String.class); // warningText
        constructor.setAccessible(true);
        OrderState orderState = constructor.newInstance(
                "Submitted",
                "INIT_MARGIN",
                "MAINT MARGIN",
                "EQUITY WITH LOAN",
                0.11,
                0.01,
                0.99,
                "COMMISSION CURRENCY",
                "WARNING TEXT");
        assertThat(IbStringUtil.toStringOrderState(orderState)).contains(
                "Order Status",
                "Submitted",
                "INIT_MARGIN",
                "MAINT MARGIN",
                "EQUITY WITH LOAN",
                "0.11",
                "0.01",
                "0.99",
                "COMMISSION CURRENCY",
                "Order Active",
                "true",
                "WARNING TEXT");
    }

    @Test
    void toStringOrder() {
        Order order = new Order();
        assertThat(IbStringUtil.toStringOrder(order)).contains(
                "Action: BUY",
                "Action Api String: BUY",
                "Order Id: 0"
        );
    }

    @Test
    void toStringOrder_null() {
        assertThat(IbStringUtil.toStringOrder(null)).isEqualTo("null");
    }

    @Test
    void toStringContract() {
        ComboLeg comboLeg1 = new ComboLeg(
                11, // conId
                7, // ratio
                "SELL", // action
                "EXCHANGE", // exchange
                2, // openClose
                4, // shortSaleSlot
                "DESIGNATED LOCATION", // designatedLocation
                99 // exemptCode
        );
        ComboLeg comboLeg2 = new ComboLeg(
                15, // conId
                9, // ratio
                "BUY", // action
                "EXCHANGE", // exchange
                1, // openClose
                1, // shortSaleSlot
                "DESIGNATED LOCATION", // designatedLocation
                55 // exemptCode
        );
        ArrayList<ComboLeg> comboLegList = new ArrayList<>();
        comboLegList.add(comboLeg1);
        comboLegList.add(comboLeg2);

        Contract contract = new Contract(
                123, // contract id
                "ABC", // symbol
                "OPT", // secType
                "20201124", // lastTradedateOrContractMonth
                11.22, // strike
                "Put", // right
                "XXX", // multiplier
                "XXX", // exchange
                "DOLLARS", // currency
                "ABC", // localSymbol
                "XXX", // tradingClass
                comboLegList, // comboLegs
                "EXCHANGE", // primaryExch
                true, // includeExpired
                "RIC", // secIdType
                "XXX" // secId
        );

        DeltaNeutralContract deltaNeutralContract1 = new DeltaNeutralContract(888, 2.2, 22.33);
        contract.underComp(deltaNeutralContract1);

        System.out.println(IbStringUtil.toStringContract(contract));
        assertThat(IbStringUtil.toStringContract(contract)).contains(
                "Symbol: ABC",
                "Security Type: OPT",
                "Expiration Date: 20201124",
                "Strike Price: 11.22",
                "Right: Put",
                "Security Id Type: RIC",
                "Leg 0",
                "Contract Id: 123",
                "Delta Neutral Contract"
        );
    }

    @Test
    void toStringContract_null() {
        assertThat(IbStringUtil.toStringContract(null)).isEqualTo("null");
    }
}
