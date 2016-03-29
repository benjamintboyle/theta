package brokers.interactive_brokers;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;

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
}
