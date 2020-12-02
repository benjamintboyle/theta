package brokers.interactive_brokers.util;

import com.ib.client.*;
import com.ib.client.Types.SecType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;

public class IbStringUtil {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String DELIMITER = ", ";
    private static final String CONTRACT_LABEL = "Contract Id: ";

    private IbStringUtil() {
    }

    /**
     * Convert an OrderState into a String.
     *
     * @param orderState An Interactive Brokers OrderState
     * @return A String representing an IB OrderState
     */
    public static String toStringOrderState(OrderState orderState) {

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Order Status: [");

        if (orderState != null) {
            stringBuilder.append("Order State: ");
            stringBuilder.append(orderState.status());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Initial Margin: ");
            stringBuilder.append(orderState.initMargin());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Maintenance Margin: ");
            stringBuilder.append(orderState.maintMargin());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Equity with Loan: ");
            stringBuilder.append(orderState.equityWithLoan());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Commission: ");
            stringBuilder.append(orderState.commission());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Min Commission: ");
            stringBuilder.append(orderState.minCommission());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Max Commission: ");
            stringBuilder.append(orderState.maxCommission());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Commission Currency: ");
            stringBuilder.append(orderState.commissionCurrency());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Order Active: ");
            stringBuilder.append(orderState.status().isActive());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Warning Text: ");
            stringBuilder.append(orderState.warningText());
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    /**
     * Convert Interactive Brokers Order to a String.
     *
     * @param order An Interactive Brokers Order.
     * @return A String representing an IB Order.
     */
    public static String toStringOrder(Order order) {

        final StringBuilder stringBuilder = new StringBuilder();

        if (order != null) {
            stringBuilder.append("Action: ");
            stringBuilder.append(order.action());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Action Api String: ");
            stringBuilder.append(order.action().getApiString());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("All or None: ");
            stringBuilder.append(Objects.toString(order.allOrNone()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Block Order: ");
            stringBuilder.append(Objects.toString(order.blockOrder()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("eTrade Only: ");
            stringBuilder.append(Objects.toString(order.eTradeOnly()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Firm Quote Only: ");
            stringBuilder.append(Objects.toString(order.firmQuoteOnly()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Hidden: ");
            stringBuilder.append(Objects.toString(order.hidden()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Not Held: ");
            stringBuilder.append(Objects.toString(order.notHeld()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Opt Out Smart Routing: ");
            stringBuilder.append(Objects.toString(order.optOutSmartRouting()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Outside Rth: ");
            stringBuilder.append(Objects.toString(order.outsideRth()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Override Percentage Constraints: ");
            stringBuilder.append(Objects.toString(order.overridePercentageConstraints()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Auto Reset: ");
            stringBuilder.append(Objects.toString(order.scaleAutoReset()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Random Percent: ");
            stringBuilder.append(Objects.toString(order.scaleRandomPercent()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Sweep To Fill: ");
            stringBuilder.append(Objects.toString(order.sweepToFill()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Transmit: ");
            stringBuilder.append(Objects.toString(order.transmit()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("WhatIf: ");
            stringBuilder.append(Objects.toString(order.whatIf()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Aux Price: ");
            stringBuilder.append(Objects.toString(order.auxPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Delta: ");
            stringBuilder.append(Objects.toString(order.delta()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Delta Neutral Aux Price: ");
            stringBuilder.append(Objects.toString(order.deltaNeutralAuxPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Discretionary Amt: ");
            stringBuilder.append(Objects.toString(order.discretionaryAmt()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Limit Price: ");
            stringBuilder.append(Objects.toString(order.lmtPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("NBBO Price Cap: ");
            stringBuilder.append(Objects.toString(order.nbboPriceCap()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Percent Offset: ");
            stringBuilder.append(Objects.toString(order.percentOffset()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Price Adjust Value: ");
            stringBuilder.append(Objects.toString(order.scalePriceAdjustValue()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Price Increment: ");
            stringBuilder.append(Objects.toString(order.scalePriceIncrement()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Profile Offset: ");
            stringBuilder.append(Objects.toString(order.scaleProfitOffset()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Starting Price: ");
            stringBuilder.append(Objects.toString(order.startingPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Stock Range Lower: ");
            stringBuilder.append(Objects.toString(order.stockRangeLower()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Stock Range Upper: ");
            stringBuilder.append(Objects.toString(order.stockRangeUpper()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Stock Ref Price: ");
            stringBuilder.append(Objects.toString(order.stockRefPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Trailing Percent: ");
            stringBuilder.append(Objects.toString(order.trailingPercent()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Trailing Stop Price: ");
            stringBuilder.append(Objects.toString(order.trailStopPrice()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Volatility: ");
            stringBuilder.append(Objects.toString(order.volatility()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Client Id: ");
            stringBuilder.append(Objects.toString(order.clientId()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Continuous Update: ");
            stringBuilder.append(Objects.toString(order.continuousUpdate()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Delta Neutral Con Id: ");
            stringBuilder.append(Objects.toString(order.deltaNeutralConId()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Display Size: ");
            stringBuilder.append(Objects.toString(order.displaySize()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Min Quantity: ");
            stringBuilder.append(Objects.toString(order.minQty()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Order Id: ");
            stringBuilder.append(Objects.toString(order.orderId()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Parent Id: ");
            stringBuilder.append(Objects.toString(order.parentId()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Init Fill Quantity: ");
            stringBuilder.append(Objects.toString(order.scaleInitFillQty()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Init Level Size: ");
            stringBuilder.append(Objects.toString(order.scaleInitLevelSize()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Init Position: ");
            stringBuilder.append(Objects.toString(order.scaleInitPosition()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Price Adjust Interval: ");
            stringBuilder.append(Objects.toString(order.scalePriceAdjustInterval()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Subs Level Size: ");
            stringBuilder.append(Objects.toString(order.scaleSubsLevelSize()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Total Quantity: ");
            stringBuilder.append(Objects.toString(order.totalQuantity()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Perm Id: ");
            stringBuilder.append(Objects.toString(order.permId()));

            stringBuilder.append(DELIMITER);
            stringBuilder.append("FA Method: ");
            stringBuilder.append(order.faMethod());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("OCA Type: ");
            stringBuilder.append(order.ocaType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Delta Neutral Order Type: ");
            stringBuilder.append(order.deltaNeutralOrderType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Order Type: ");
            stringBuilder.append(order.orderType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Reference Price Type: ");
            stringBuilder.append(order.referencePriceType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Rule SOA: ");
            stringBuilder.append(order.rule80A());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Account: ");
            stringBuilder.append(order.account());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Algo Strategy: ");
            stringBuilder.append(order.algoStrategy());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Algo Id: ");
            stringBuilder.append(order.algoId());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("FA Group: ");
            stringBuilder.append(order.faGroup());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("FA Percentage: ");
            stringBuilder.append(order.faPercentage());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("FA Profile: ");
            stringBuilder.append(order.faProfile());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Good After Time: ");
            stringBuilder.append(order.goodAfterTime());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Good Till Date: ");
            stringBuilder.append(order.goodTillDate());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Hedge Param: ");
            stringBuilder.append(order.hedgeParam());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("OCA Group: ");
            stringBuilder.append(order.ocaGroup());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Order Ref: ");
            stringBuilder.append(order.orderRef());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("TIF: ");
            stringBuilder.append(order.tif());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Volatility Type: ");
            stringBuilder.append(order.volatilityType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Smart Combo Routing Params: ");
            stringBuilder.append(order.smartComboRoutingParams());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Trigger Method: ");
            stringBuilder.append(order.triggerMethod());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Algo Params: ");
            stringBuilder.append(order.algoParams());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Order Combo Legs: ");
            stringBuilder.append(order.orderComboLegs());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Scale Table: ");
            stringBuilder.append(order.scaleTable());
        } else {
            stringBuilder.append("null");
            logger.warn("Attempted to display String for null Order");
        }

        return stringBuilder.toString();
    }

    /**
     * Convert Interactive Brokers Contract to String.
     *
     * @param contract An Interactive Brokers Contract.
     * @return A String representing an IB Contract.
     */
    public static String toStringContract(Contract contract) {

        final StringBuilder stringBuilder = new StringBuilder();

        if (contract != null) {

            stringBuilder.append("Symbol: ");
            stringBuilder.append(contract.symbol());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Security Type: ");
            stringBuilder.append(contract.secType());


            if (!contract.secType().equals(SecType.STK)) {
                stringBuilder.append(DELIMITER);
                stringBuilder.append("Expiration Date: ");
                stringBuilder.append(contract.lastTradeDateOrContractMonth());

                stringBuilder.append(DELIMITER);
                stringBuilder.append("Strike Price: ");
                stringBuilder.append(contract.strike());
            }


            stringBuilder.append(DELIMITER);
            stringBuilder.append("Right: ");
            stringBuilder.append(contract.right());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Multiplier: ");
            stringBuilder.append(contract.multiplier());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Primary Exchange: ");
            stringBuilder.append(contract.primaryExch());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Currency: ");
            stringBuilder.append(contract.currency());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Local Symbol: ");
            stringBuilder.append(contract.localSymbol());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Trading Class: ");
            stringBuilder.append(contract.tradingClass());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Security Id Type: ");
            stringBuilder.append(contract.secIdType());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Security Id: ");
            stringBuilder.append(contract.secId());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Include Expired: ");
            stringBuilder.append(contract.includeExpired());


            if (contract.underComp() != null) {
                stringBuilder.append(DELIMITER);
                stringBuilder.append("Delta Neutral Contract: ");
                stringBuilder.append("[");
                stringBuilder.append(IbStringUtil.toStringDeltaNeutralContract(contract.underComp()));
                stringBuilder.append("]");
            }


            final List<ComboLeg> comboLegList = contract.comboLegs();

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Combo Legs: ");
            stringBuilder.append("[ ");
            stringBuilder.append("Count: ");
            stringBuilder.append(comboLegList.size());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Combo Legs Description: ");
            stringBuilder.append(contract.comboLegsDescrip());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("[ ");

            if (!comboLegList.isEmpty()) {

                for (int i = 0; i < comboLegList.size(); i++) {

                    if (i > 0) {
                        stringBuilder.append(DELIMITER);
                    }

                    stringBuilder.append("Leg ");
                    stringBuilder.append(i);
                    stringBuilder.append(": [ ");

                    stringBuilder.append(IbStringUtil.toStringComboLeg(comboLegList.get(i)));

                    stringBuilder.append(" ]");
                }
            }

            stringBuilder.append("]]");

            stringBuilder.append(DELIMITER);
            stringBuilder.append(CONTRACT_LABEL);
            stringBuilder.append(contract.conid());

        } else {
            stringBuilder.append("null");
            logger.warn("Attempted to display String for null Contract");
        }

        return stringBuilder.toString();
    }

    /**
     * Converts an Interactive Brokers Delta Neutral Contract to a String.
     *
     * @param deltaNeutralContract An Interactive Brokers Delta Neutral Contract.
     * @return A String representing an IB Delta Neutral Contract.
     */
    private static String toStringDeltaNeutralContract(DeltaNeutralContract deltaNeutralContract) {

        final StringBuilder stringBuilder = new StringBuilder();

        if (deltaNeutralContract != null) {
            stringBuilder.append(CONTRACT_LABEL);
            stringBuilder.append(deltaNeutralContract.conid());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Delta: ");
            stringBuilder.append(deltaNeutralContract.delta());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Price: ");
            stringBuilder.append(deltaNeutralContract.price());
        }

        return stringBuilder.toString();
    }

    /**
     * Converts an Interactive Brokers Combo Leg into a String.
     *
     * @param comboLeg An Interactive Brokers Combo Leg.
     * @return A String representing an IB Combo Leg.
     */
    private static String toStringComboLeg(ComboLeg comboLeg) {

        final StringBuilder stringBuilder = new StringBuilder();

        if (comboLeg != null) {
            stringBuilder.append(CONTRACT_LABEL);
            stringBuilder.append(comboLeg.conid());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Ratio: ");
            stringBuilder.append(comboLeg.ratio());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Action: ");
            stringBuilder.append(comboLeg.action());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Exchange: ");
            stringBuilder.append(comboLeg.exchange());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Open/Close: ");
            stringBuilder.append(comboLeg.openClose());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Short Sale Slot: ");
            stringBuilder.append(comboLeg.shortSaleSlot());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Designated Location: ");
            stringBuilder.append(comboLeg.designatedLocation());

            stringBuilder.append(DELIMITER);
            stringBuilder.append("Exempt Code: ");
            stringBuilder.append(comboLeg.exemptCode());
        }

        return stringBuilder.toString();
    }
}
