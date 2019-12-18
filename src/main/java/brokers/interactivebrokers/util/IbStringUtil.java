package brokers.interactivebrokers.util;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.Types.SecType;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbStringUtil {

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String DELIMITTER = ", ";
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

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Initial Margin: ");
      stringBuilder.append(orderState.initMargin());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Maintenance Margin: ");
      stringBuilder.append(orderState.maintMargin());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Equity with Loan: ");
      stringBuilder.append(orderState.equityWithLoan());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Commission: ");
      stringBuilder.append(orderState.commission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Min Commission: ");
      stringBuilder.append(orderState.minCommission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Max Commission: ");
      stringBuilder.append(orderState.maxCommission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Commission Currency: ");
      stringBuilder.append(orderState.commissionCurrency());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Active: ");
      stringBuilder.append(orderState.status().isActive());

      stringBuilder.append(DELIMITTER);
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
      stringBuilder.append(Objects.toString(order.action()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Action Api String: ");
      stringBuilder.append(Objects.toString(order.action().getApiString()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("All or None: ");
      stringBuilder.append(Objects.toString(order.allOrNone()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Block Order: ");
      stringBuilder.append(Objects.toString(order.blockOrder()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("eTrade Only: ");
      stringBuilder.append(Objects.toString(order.eTradeOnly()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Firm Quote Only: ");
      stringBuilder.append(Objects.toString(order.firmQuoteOnly()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Hidden: ");
      stringBuilder.append(Objects.toString(order.hidden()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Not Held: ");
      stringBuilder.append(Objects.toString(order.notHeld()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Opt Out Smart Routing: ");
      stringBuilder.append(Objects.toString(order.optOutSmartRouting()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Outside Rth: ");
      stringBuilder.append(Objects.toString(order.outsideRth()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Override Percentage Constraints: ");
      stringBuilder.append(Objects.toString(order.overridePercentageConstraints()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Auto Reset: ");
      stringBuilder.append(Objects.toString(order.scaleAutoReset()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Random Percent: ");
      stringBuilder.append(Objects.toString(order.scaleRandomPercent()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Sweep To Fill: ");
      stringBuilder.append(Objects.toString(order.sweepToFill()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Transmit: ");
      stringBuilder.append(Objects.toString(order.transmit()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("WhatIf: ");
      stringBuilder.append(Objects.toString(order.whatIf()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Aux Price: ");
      stringBuilder.append(Objects.toString(order.auxPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta: ");
      stringBuilder.append(Objects.toString(order.delta()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Aux Price: ");
      stringBuilder.append(Objects.toString(order.deltaNeutralAuxPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Discretionary Amt: ");
      stringBuilder.append(Objects.toString(order.discretionaryAmt()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Limit Price: ");
      stringBuilder.append(Objects.toString(order.lmtPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("NBBO Price Cap: ");
      stringBuilder.append(Objects.toString(order.nbboPriceCap()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Percent Offset: ");
      stringBuilder.append(Objects.toString(order.percentOffset()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Adjust Value: ");
      stringBuilder.append(Objects.toString(order.scalePriceAdjustValue()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Increment: ");
      stringBuilder.append(Objects.toString(order.scalePriceIncrement()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Profile Offset: ");
      stringBuilder.append(Objects.toString(order.scaleProfitOffset()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Starting Price: ");
      stringBuilder.append(Objects.toString(order.startingPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Range Lower: ");
      stringBuilder.append(Objects.toString(order.stockRangeLower()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Range Upper: ");
      stringBuilder.append(Objects.toString(order.stockRangeUpper()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Ref Price: ");
      stringBuilder.append(Objects.toString(order.stockRefPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trailing Percent: ");
      stringBuilder.append(Objects.toString(order.trailingPercent()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trailing Stop Price: ");
      stringBuilder.append(Objects.toString(order.trailStopPrice()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Volatility: ");
      stringBuilder.append(Objects.toString(order.volatility()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Client Id: ");
      stringBuilder.append(Objects.toString(order.clientId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Continuous Update: ");
      stringBuilder.append(Objects.toString(order.continuousUpdate()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Con Id: ");
      stringBuilder.append(Objects.toString(order.deltaNeutralConId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Display Size: ");
      stringBuilder.append(Objects.toString(order.displaySize()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Min Quantity: ");
      stringBuilder.append(Objects.toString(order.minQty()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Id: ");
      stringBuilder.append(Objects.toString(order.orderId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Parent Id: ");
      stringBuilder.append(Objects.toString(order.parentId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Fill Quantity: ");
      stringBuilder.append(Objects.toString(order.scaleInitFillQty()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Level Size: ");
      stringBuilder.append(Objects.toString(order.scaleInitLevelSize()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Position: ");
      stringBuilder.append(Objects.toString(order.scaleInitPosition()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Adjust Interval: ");
      stringBuilder.append(Objects.toString(order.scalePriceAdjustInterval()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Subs Level Size: ");
      stringBuilder.append(Objects.toString(order.scaleSubsLevelSize()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Total Quantity: ");
      stringBuilder.append(Objects.toString(order.totalQuantity()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Perm Id: ");
      stringBuilder.append(Objects.toString(order.permId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Method: ");
      stringBuilder.append(Objects.toString(order.faMethod()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("OCA Type: ");
      stringBuilder.append(Objects.toString(order.ocaType()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Order Type: ");
      stringBuilder.append(Objects.toString(order.deltaNeutralOrderType()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Type: ");
      stringBuilder.append(Objects.toString(order.orderType()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Reference Price Type: ");
      stringBuilder.append(Objects.toString(order.referencePriceType()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Rule SOA: ");
      stringBuilder.append(Objects.toString(order.rule80A()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Account: ");
      stringBuilder.append(Objects.toString(order.account()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Strategy: ");
      stringBuilder.append(Objects.toString(order.algoStrategy()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Id: ");
      stringBuilder.append(Objects.toString(order.algoId()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Group: ");
      stringBuilder.append(Objects.toString(order.faGroup()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Percentage: ");
      stringBuilder.append(Objects.toString(order.faPercentage()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Profile: ");
      stringBuilder.append(Objects.toString(order.faProfile()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Good After Time: ");
      stringBuilder.append(Objects.toString(order.goodAfterTime()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Good Till Date: ");
      stringBuilder.append(Objects.toString(order.goodTillDate()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Hedge Param: ");
      stringBuilder.append(Objects.toString(order.hedgeParam()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("OCA Group: ");
      stringBuilder.append(Objects.toString(order.ocaGroup()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Ref: ");
      stringBuilder.append(Objects.toString(order.orderRef()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("TIF: ");
      stringBuilder.append(Objects.toString(order.tif()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Volatility Type: ");
      stringBuilder.append(Objects.toString(order.volatilityType()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Smart Combo Routing Params: ");
      stringBuilder.append(Objects.toString(order.smartComboRoutingParams()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trigger Method: ");
      stringBuilder.append(Objects.toString(order.triggerMethod()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Params: ");
      stringBuilder.append(Objects.toString(order.algoParams()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Combo Legs: ");
      stringBuilder.append(Objects.toString(order.orderComboLegs()));

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Table: ");
      stringBuilder.append(Objects.toString(order.scaleTable()));
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

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Security Type: ");
      stringBuilder.append(contract.secType());


      if (!contract.secType().equals(SecType.STK)) {
        stringBuilder.append(DELIMITTER);
        stringBuilder.append("Expiration Date: ");
        stringBuilder.append(contract.lastTradeDateOrContractMonth());

        stringBuilder.append(DELIMITTER);
        stringBuilder.append("Strike Price: ");
        stringBuilder.append(contract.strike());
      }


      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Right: ");
      stringBuilder.append(contract.right());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Multiplier: ");
      stringBuilder.append(contract.multiplier());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Primary Exchange: ");
      stringBuilder.append(contract.primaryExch());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Currency: ");
      stringBuilder.append(contract.currency());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Local Symbol: ");
      stringBuilder.append(contract.localSymbol());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trading Class: ");
      stringBuilder.append(contract.tradingClass());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Security Id Type: ");
      stringBuilder.append(contract.secIdType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Security Id: ");
      stringBuilder.append(contract.secId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Include Expired: ");
      stringBuilder.append(contract.includeExpired());


      if (contract.underComp() != null) {
        stringBuilder.append(DELIMITTER);
        stringBuilder.append("Delta Neutral Contract: ");
        stringBuilder.append("[");
        stringBuilder.append(IbStringUtil.toStringDeltaNeutralContract(contract.underComp()));
        stringBuilder.append("]");
      }


      final List<ComboLeg> comboLegList = contract.comboLegs();

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Combo Legs: ");
      stringBuilder.append("[ ");
      stringBuilder.append("Count: ");
      stringBuilder.append(comboLegList.size());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Combo Legs Description: ");
      stringBuilder.append(contract.comboLegsDescrip());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Legs: [ ");

      if (!comboLegList.isEmpty()) {

        for (int i = 0; i < comboLegList.size(); i++) {

          if (i > 0) {
            stringBuilder.append(DELIMITTER);
          }

          stringBuilder.append("Leg ");
          stringBuilder.append(i);
          stringBuilder.append(": [ ");

          IbStringUtil.toStringComboLeg(comboLegList.get(i));

          stringBuilder.append(" ] ");
        }
      }

      stringBuilder.append(" ] ]");


      stringBuilder.append(DELIMITTER);
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
  public static String toStringDeltaNeutralContract(DeltaNeutralContract deltaNeutralContract) {

    final StringBuilder stringBuilder = new StringBuilder();

    if (deltaNeutralContract != null) {
      stringBuilder.append(CONTRACT_LABEL);
      stringBuilder.append(deltaNeutralContract.conid());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta: ");
      stringBuilder.append(deltaNeutralContract.delta());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Price: ");
      stringBuilder.append(deltaNeutralContract.price());
    }

    if (stringBuilder.length() == 0) {
      stringBuilder.append(Objects.toString(deltaNeutralContract));
    }

    return stringBuilder.toString();
  }

  /**
   * Converts an Interactive Brokers Combo Leg into a String.
   *
   * @param comboLeg An Interactive Brokers Combo Leg.
   * @return A String representing an IB Combo Leg.
   */
  public static String toStringComboLeg(ComboLeg comboLeg) {

    final StringBuilder stringBuilder = new StringBuilder();

    if (comboLeg != null) {
      stringBuilder.append(DELIMITTER);
      stringBuilder.append(CONTRACT_LABEL);
      stringBuilder.append(comboLeg.conid());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Ratio: ");
      stringBuilder.append(comboLeg.ratio());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Action: ");
      stringBuilder.append(comboLeg.action());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Exchange: ");
      stringBuilder.append(comboLeg.exchange());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Open/Close: ");
      stringBuilder.append(comboLeg.openClose());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Short Sale Slot: ");
      stringBuilder.append(comboLeg.shortSaleSlot());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Designated Location: ");
      stringBuilder.append(comboLeg.designatedLocation());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Exempt Code: ");
      stringBuilder.append(comboLeg.exemptCode());
    }

    if (stringBuilder.length() == 0) {
      stringBuilder.append(Objects.toString(comboLeg));
    }

    return stringBuilder.toString();
  }
}
