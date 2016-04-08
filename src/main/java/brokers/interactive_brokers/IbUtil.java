package brokers.interactive_brokers;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;

public class IbUtil {
	public static String contractToString(Contract contract) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Contract Id: ");
		stringBuilder.append(contract.m_conId);

		stringBuilder.append(", Symbol: ");
		stringBuilder.append(contract.m_symbol);

		stringBuilder.append(", Security Type: ");
		stringBuilder.append(contract.m_secType);

		stringBuilder.append(", Expiry: ");
		stringBuilder.append(contract.m_expiry);

		stringBuilder.append(", Strike: ");
		stringBuilder.append(contract.m_strike);

		stringBuilder.append(", Right: ");
		stringBuilder.append(contract.m_right);

		stringBuilder.append(", Multiplier: ");
		stringBuilder.append(contract.m_multiplier);

		stringBuilder.append(", Exchange: ");
		stringBuilder.append(contract.m_exchange);

		stringBuilder.append(", Currency: ");
		stringBuilder.append(contract.m_currency);

		stringBuilder.append(", Local Symbol: ");
		stringBuilder.append(contract.m_localSymbol);

		stringBuilder.append(", Trading Class: ");
		stringBuilder.append(contract.m_tradingClass);

		stringBuilder.append(", Primary Exchange: ");
		stringBuilder.append(contract.m_primaryExch);

		stringBuilder.append(", Include Expired: ");
		stringBuilder.append(contract.m_includeExpired);

		stringBuilder.append(", Security Id Type: ");
		stringBuilder.append(contract.m_secIdType);

		stringBuilder.append(", Security Id: ");
		stringBuilder.append(contract.m_secId);

		stringBuilder.append(", Combo Leg Description: ");
		stringBuilder.append(contract.m_comboLegsDescrip);

		stringBuilder.append(", Combo Legs: { ");
		for (ComboLeg leg : contract.m_comboLegs) {
			stringBuilder.append("[Contract Id: ");
			stringBuilder.append(leg.m_conId);

			stringBuilder.append(", Ratio: ");
			stringBuilder.append(leg.m_ratio);

			stringBuilder.append(", Action: ");
			stringBuilder.append(leg.m_action);

			stringBuilder.append(", Exchange: ");
			stringBuilder.append(leg.m_exchange);

			stringBuilder.append(", Open/Close: ");
			stringBuilder.append(leg.m_openClose);

			stringBuilder.append(", Short Sale Slot: ");
			stringBuilder.append(leg.m_shortSaleSlot);

			stringBuilder.append(", Designated Location: ");
			stringBuilder.append(leg.m_designatedLocation);

			stringBuilder.append(", Exempt Code: ");
			stringBuilder.append(leg.m_exemptCode);

			stringBuilder.append("]");
		}

		stringBuilder.append(" }, Under Comp: { ");

		if (contract.m_underComp != null) {
			stringBuilder.append("Contract Id: ");
			stringBuilder.append(contract.m_underComp.m_conId);
			stringBuilder.append(", Delta: ");
			stringBuilder.append(contract.m_underComp.m_delta);
			stringBuilder.append(", Price: ");
			stringBuilder.append(contract.m_underComp.m_price);
		}

		stringBuilder.append(" }");

		return stringBuilder.toString();
	}

	public static String newOrderStateToString(NewOrderState newOrderState) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Commission: ");
		stringBuilder.append(newOrderState.commission());

		stringBuilder.append(", Max Commission: ");
		stringBuilder.append(newOrderState.maxCommission());

		stringBuilder.append(", Min Commission: ");
		stringBuilder.append(newOrderState.minCommission());

		stringBuilder.append(", Status: ");
		stringBuilder.append(newOrderState.status());

		stringBuilder.append(", Order Active: ");
		stringBuilder.append(newOrderState.status().isActive());

		stringBuilder.append(", Commission Currency: ");
		stringBuilder.append(newOrderState.commissionCurrency());

		stringBuilder.append(", Equity with Loan: ");
		stringBuilder.append(newOrderState.equityWithLoan());

		stringBuilder.append(", Initial Margin: ");
		stringBuilder.append(newOrderState.initMargin());

		stringBuilder.append(", Maintenance Margin: ");
		stringBuilder.append(newOrderState.maintMargin());

		stringBuilder.append(", Warning Text: ");
		stringBuilder.append(newOrderState.warningText());

		return stringBuilder.toString();
	}

	public static String newOrderToString(NewOrder newOrder) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("Action: ");
		stringBuilder.append(newOrder.action());

		stringBuilder.append(", Action Api String: ");
		stringBuilder.append(newOrder.action().getApiString());

		stringBuilder.append(", All or None: ");
		stringBuilder.append(newOrder.allOrNone());

		stringBuilder.append(", Block Order: ");
		stringBuilder.append(newOrder.blockOrder());

		stringBuilder.append(", eTrade Only: ");
		stringBuilder.append(newOrder.eTradeOnly());

		stringBuilder.append(", Firm Quote Only: ");
		stringBuilder.append(newOrder.firmQuoteOnly());

		stringBuilder.append(", Hidden: ");
		stringBuilder.append(newOrder.hidden());

		stringBuilder.append(", Not Held: ");
		stringBuilder.append(newOrder.notHeld());

		stringBuilder.append(", Opt Out Smart Routing: ");
		stringBuilder.append(newOrder.optOutSmartRouting());

		stringBuilder.append(", Outside Rth: ");
		stringBuilder.append(newOrder.outsideRth());

		stringBuilder.append(", Override Percentage Constraints: ");
		stringBuilder.append(newOrder.overridePercentageConstraints());

		stringBuilder.append(", Scale Auto Reset: ");
		stringBuilder.append(newOrder.scaleAutoReset());

		stringBuilder.append(", Scale Random Percent: ");
		stringBuilder.append(newOrder.scaleRandomPercent());

		stringBuilder.append(", Sweep To Fill: ");
		stringBuilder.append(newOrder.sweepToFill());

		stringBuilder.append(", Transmit: ");
		stringBuilder.append(newOrder.transmit());

		stringBuilder.append(", WhatIf: ");
		stringBuilder.append(newOrder.whatIf());

		stringBuilder.append(", Aux Price: ");
		stringBuilder.append(newOrder.auxPrice());

		stringBuilder.append(", Delta: ");
		stringBuilder.append(newOrder.delta());

		stringBuilder.append(", Delta Neutral Aux Price: ");
		stringBuilder.append(newOrder.deltaNeutralAuxPrice());

		stringBuilder.append(", Discretionary Amt: ");
		stringBuilder.append(newOrder.discretionaryAmt());

		stringBuilder.append(", Limit Price: ");
		stringBuilder.append(newOrder.lmtPrice());

		stringBuilder.append(", NBBO Price Cap: ");
		stringBuilder.append(newOrder.nbboPriceCap());

		stringBuilder.append(", Percent Offset: ");
		stringBuilder.append(newOrder.percentOffset());

		stringBuilder.append(", Scale Price Adjust Value: ");
		stringBuilder.append(newOrder.scalePriceAdjustValue());

		stringBuilder.append(", Scale Price Increment: ");
		stringBuilder.append(newOrder.scalePriceIncrement());

		stringBuilder.append(", Scale Profile Offset: ");
		stringBuilder.append(newOrder.scaleProfitOffset());

		stringBuilder.append(", Starting Price: ");
		stringBuilder.append(newOrder.startingPrice());

		stringBuilder.append(", Stock Range Lower: ");
		stringBuilder.append(newOrder.stockRangeLower());

		stringBuilder.append(", Stock Range Upper: ");
		stringBuilder.append(newOrder.stockRangeUpper());

		stringBuilder.append(", Stock Ref Price: ");
		stringBuilder.append(newOrder.stockRefPrice());

		stringBuilder.append(", Trailing Percent: ");
		stringBuilder.append(newOrder.trailingPercent());

		stringBuilder.append(", Trailing Stop Price: ");
		stringBuilder.append(newOrder.trailStopPrice());

		stringBuilder.append(", Volatility: ");
		stringBuilder.append(newOrder.volatility());

		stringBuilder.append(", Client Id: ");
		stringBuilder.append(newOrder.clientId());

		stringBuilder.append(", Continuous Update: ");
		stringBuilder.append(newOrder.continuousUpdate());

		stringBuilder.append(", Delta Neutral Con Id: ");
		stringBuilder.append(newOrder.deltaNeutralConId());

		stringBuilder.append(", Display Size: ");
		stringBuilder.append(newOrder.displaySize());

		stringBuilder.append(", Min Quantity: ");
		stringBuilder.append(newOrder.minQty());

		stringBuilder.append(", Order Id: ");
		stringBuilder.append(newOrder.orderId());

		stringBuilder.append(", Parent Id: ");
		stringBuilder.append(newOrder.parentId());

		stringBuilder.append(", Scale Init Fill Quantity: ");
		stringBuilder.append(newOrder.scaleInitFillQty());

		stringBuilder.append(", Scale Init Level Size: ");
		stringBuilder.append(newOrder.scaleInitLevelSize());

		stringBuilder.append(", Scale Init Position: ");
		stringBuilder.append(newOrder.scaleInitPosition());

		stringBuilder.append(", Scale Price Adjust Interval: ");
		stringBuilder.append(newOrder.scalePriceAdjustInterval());

		stringBuilder.append(", Scale Subs Level Size: ");
		stringBuilder.append(newOrder.scaleSubsLevelSize());

		stringBuilder.append(", Total Quantity: ");
		stringBuilder.append(newOrder.totalQuantity());

		stringBuilder.append(", Perm Id: ");
		stringBuilder.append(newOrder.permId());

		stringBuilder.append(", FA Method: ");
		stringBuilder.append(newOrder.faMethod());

		stringBuilder.append(", OCA Type: ");
		stringBuilder.append(newOrder.ocaType());

		stringBuilder.append(", Delta Neutral Order Type: ");
		stringBuilder.append(newOrder.deltaNeutralOrderType());

		stringBuilder.append(", Order Type: ");
		stringBuilder.append(newOrder.orderType());

		stringBuilder.append(", Reference Price Type: ");
		stringBuilder.append(newOrder.referencePriceType());

		stringBuilder.append(", Rule SOA: ");
		stringBuilder.append(newOrder.rule80A());

		stringBuilder.append(", Account: ");
		stringBuilder.append(newOrder.account());

		stringBuilder.append(", Algo Strategy: ");
		stringBuilder.append(newOrder.algoStrategy());

		stringBuilder.append(", Algo Id: ");
		stringBuilder.append(newOrder.algoId());

		stringBuilder.append(", FA Group: ");
		stringBuilder.append(newOrder.faGroup());

		stringBuilder.append(", FA Percentage: ");
		stringBuilder.append(newOrder.faPercentage());

		stringBuilder.append(", FA Profile: ");
		stringBuilder.append(newOrder.faProfile());

		stringBuilder.append(", Good After Time: ");
		stringBuilder.append(newOrder.goodAfterTime());

		stringBuilder.append(", Good Till Date: ");
		stringBuilder.append(newOrder.goodTillDate());

		stringBuilder.append(", Hedge Param: ");
		stringBuilder.append(newOrder.hedgeParam());

		stringBuilder.append(", OCA Group: ");
		stringBuilder.append(newOrder.ocaGroup());

		stringBuilder.append(", Order Ref: ");
		stringBuilder.append(newOrder.orderRef());

		stringBuilder.append(", TIF: ");
		stringBuilder.append(newOrder.tif());

		stringBuilder.append(", Volatility Type: ");
		stringBuilder.append(newOrder.volatilityType());

		stringBuilder.append(", Smart Combo Routing Params: ");
		stringBuilder.append(newOrder.smartComboRoutingParams());

		stringBuilder.append(", Trigger Method: ");
		stringBuilder.append(newOrder.triggerMethod());

		stringBuilder.append(", Algo Params: ");
		stringBuilder.append(newOrder.algoParams());

		stringBuilder.append(", Order Combo Legs: ");
		stringBuilder.append(newOrder.orderComboLegs());

		stringBuilder.append(", Scale Table: ");
		stringBuilder.append(newOrder.scaleTable());

		return stringBuilder.toString();
	}
}
