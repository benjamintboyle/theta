package brokers.interactive_brokers.util;

import java.util.List;
import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;

public class IbStringUtil {

  private static final String DELIMITTER = ", ";

  public static String toStringOrderState(OrderState orderState) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (orderState != null) {
      stringBuilder.append("Commission: ");
      stringBuilder.append(orderState.commission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Max Commission: ");
      stringBuilder.append(orderState.maxCommission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Min Commission: ");
      stringBuilder.append(orderState.minCommission());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Status: ");
      stringBuilder.append(orderState.status());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Active: ");
      stringBuilder.append(orderState.status().isActive());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Commission Currency: ");
      stringBuilder.append(orderState.commissionCurrency());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Equity with Loan: ");
      stringBuilder.append(orderState.equityWithLoan());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Initial Margin: ");
      stringBuilder.append(orderState.initMargin());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Maintenance Margin: ");
      stringBuilder.append(orderState.maintMargin());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Warning Text: ");
      stringBuilder.append(orderState.warningText());
    }
    return stringBuilder.toString();
  }

  public static String toStringOrderStatus(OrderStatus status, double filled, double remaining,
      double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId,
      String whyHeld) {

    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("Order Status: ");
    stringBuilder.append(status);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Filled: ");
    stringBuilder.append(filled);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Remaining: ");
    stringBuilder.append(remaining);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Avg Price: ");
    stringBuilder.append(avgFillPrice);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Perm Id: ");
    stringBuilder.append(permId);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Parent Id: ");
    stringBuilder.append(parentId);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Last Fill Price: ");
    stringBuilder.append(lastFillPrice);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Client Id: ");
    stringBuilder.append(clientId);

    stringBuilder.append(DELIMITTER);
    stringBuilder.append("Why Held: ");
    stringBuilder.append(whyHeld);

    return stringBuilder.toString();
  }

  public static String toStringOrder(Order order) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (order != null) {
      stringBuilder.append("Action: ");
      stringBuilder.append(order.action());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Action Api String: ");
      stringBuilder.append(order.action().getApiString());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("All or None: ");
      stringBuilder.append(order.allOrNone());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Block Order: ");
      stringBuilder.append(order.blockOrder());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("eTrade Only: ");
      stringBuilder.append(order.eTradeOnly());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Firm Quote Only: ");
      stringBuilder.append(order.firmQuoteOnly());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Hidden: ");
      stringBuilder.append(order.hidden());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Not Held: ");
      stringBuilder.append(order.notHeld());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Opt Out Smart Routing: ");
      stringBuilder.append(order.optOutSmartRouting());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Outside Rth: ");
      stringBuilder.append(order.outsideRth());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Override Percentage Constraints: ");
      stringBuilder.append(order.overridePercentageConstraints());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Auto Reset: ");
      stringBuilder.append(order.scaleAutoReset());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Random Percent: ");
      stringBuilder.append(order.scaleRandomPercent());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Sweep To Fill: ");
      stringBuilder.append(order.sweepToFill());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Transmit: ");
      stringBuilder.append(order.transmit());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("WhatIf: ");
      stringBuilder.append(order.whatIf());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Aux Price: ");
      stringBuilder.append(order.auxPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta: ");
      stringBuilder.append(order.delta());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Aux Price: ");
      stringBuilder.append(order.deltaNeutralAuxPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Discretionary Amt: ");
      stringBuilder.append(order.discretionaryAmt());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Limit Price: ");
      stringBuilder.append(order.lmtPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("NBBO Price Cap: ");
      stringBuilder.append(order.nbboPriceCap());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Percent Offset: ");
      stringBuilder.append(order.percentOffset());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Adjust Value: ");
      stringBuilder.append(order.scalePriceAdjustValue());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Increment: ");
      stringBuilder.append(order.scalePriceIncrement());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Profile Offset: ");
      stringBuilder.append(order.scaleProfitOffset());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Starting Price: ");
      stringBuilder.append(order.startingPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Range Lower: ");
      stringBuilder.append(order.stockRangeLower());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Range Upper: ");
      stringBuilder.append(order.stockRangeUpper());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Stock Ref Price: ");
      stringBuilder.append(order.stockRefPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trailing Percent: ");
      stringBuilder.append(order.trailingPercent());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trailing Stop Price: ");
      stringBuilder.append(order.trailStopPrice());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Volatility: ");
      stringBuilder.append(order.volatility());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Client Id: ");
      stringBuilder.append(order.clientId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Continuous Update: ");
      stringBuilder.append(order.continuousUpdate());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Con Id: ");
      stringBuilder.append(order.deltaNeutralConId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Display Size: ");
      stringBuilder.append(order.displaySize());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Min Quantity: ");
      stringBuilder.append(order.minQty());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Id: ");
      stringBuilder.append(order.orderId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Parent Id: ");
      stringBuilder.append(order.parentId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Fill Quantity: ");
      stringBuilder.append(order.scaleInitFillQty());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Level Size: ");
      stringBuilder.append(order.scaleInitLevelSize());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Init Position: ");
      stringBuilder.append(order.scaleInitPosition());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Price Adjust Interval: ");
      stringBuilder.append(order.scalePriceAdjustInterval());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Subs Level Size: ");
      stringBuilder.append(order.scaleSubsLevelSize());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Total Quantity: ");
      stringBuilder.append(order.totalQuantity());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Perm Id: ");
      stringBuilder.append(order.permId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Method: ");
      stringBuilder.append(order.faMethod());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("OCA Type: ");
      stringBuilder.append(order.ocaType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta Neutral Order Type: ");
      stringBuilder.append(order.deltaNeutralOrderType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Type: ");
      stringBuilder.append(order.orderType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Reference Price Type: ");
      stringBuilder.append(order.referencePriceType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Rule SOA: ");
      stringBuilder.append(order.rule80A());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Account: ");
      stringBuilder.append(order.account());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Strategy: ");
      stringBuilder.append(order.algoStrategy());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Id: ");
      stringBuilder.append(order.algoId());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Group: ");
      stringBuilder.append(order.faGroup());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Percentage: ");
      stringBuilder.append(order.faPercentage());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("FA Profile: ");
      stringBuilder.append(order.faProfile());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Good After Time: ");
      stringBuilder.append(order.goodAfterTime());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Good Till Date: ");
      stringBuilder.append(order.goodTillDate());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Hedge Param: ");
      stringBuilder.append(order.hedgeParam());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("OCA Group: ");
      stringBuilder.append(order.ocaGroup());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Ref: ");
      stringBuilder.append(order.orderRef());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("TIF: ");
      stringBuilder.append(order.tif());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Volatility Type: ");
      stringBuilder.append(order.volatilityType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Smart Combo Routing Params: ");
      stringBuilder.append(order.smartComboRoutingParams());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Trigger Method: ");
      stringBuilder.append(order.triggerMethod());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Algo Params: ");
      stringBuilder.append(order.algoParams());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Order Combo Legs: ");
      stringBuilder.append(order.orderComboLegs());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Scale Table: ");
      stringBuilder.append(order.scaleTable());
    }
    return stringBuilder.toString();

  }

  public static String toStringContract(Contract contract) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (contract != null) {
      stringBuilder.append("Contract Id: ");
      stringBuilder.append(contract.conid());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Symbol: ");
      stringBuilder.append(contract.symbol());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Security Type: ");
      stringBuilder.append(contract.secType());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Expiration Date: ");
      stringBuilder.append(contract.lastTradeDateOrContractMonth());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Strike Price: ");
      stringBuilder.append(contract.strike());

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
      stringBuilder.append("Delta Neutral Contract: ");
      stringBuilder.append("[");
      stringBuilder.append(IbStringUtil.toStringDeltaNeutralContract(contract.underComp()));
      stringBuilder.append("]");

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Include Expired: ");
      stringBuilder.append(contract.includeExpired());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Combo Legs Description: ");
      stringBuilder.append(contract.comboLegsDescrip());

      final List<ComboLeg> comboLegList = contract.comboLegs();

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Combo Legs: ");
      stringBuilder.append("[");
      stringBuilder.append("Count: ");
      stringBuilder.append(comboLegList.size());

      for (int i = 0; i < comboLegList.size(); i++) {
        stringBuilder.append(DELIMITTER);
        stringBuilder.append(i);
        stringBuilder.append(": [");

        IbStringUtil.toStringComboLeg(comboLegList.get(i));

        stringBuilder.append("]");
      }

      stringBuilder.append("]");
    }

    return stringBuilder.toString();
  }

  public static String toStringDeltaNeutralContract(DeltaNeutralContract deltaNeutralContract) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (deltaNeutralContract != null) {
      stringBuilder.append("Contract Id: ");
      stringBuilder.append(deltaNeutralContract.conid());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Delta: ");
      stringBuilder.append(deltaNeutralContract.delta());

      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Price: ");
      stringBuilder.append(deltaNeutralContract.price());
    }

    return stringBuilder.toString();
  }

  public static String toStringComboLeg(ComboLeg comboLeg) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (comboLeg != null) {
      stringBuilder.append(DELIMITTER);
      stringBuilder.append("Contract Id: ");
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

    return stringBuilder.toString();
  }
}
