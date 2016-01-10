package brokers.interactive_brokers;

import com.ib.controller.ApiController.IDeepMktDataHandler;
import com.ib.controller.Types.DeepSide;
import com.ib.controller.Types.DeepType;

public class IbMarketDepthHandler implements IDeepMktDataHandler {

	@Override
	public void updateMktDepth(int position, String marketMaker, DeepType operation, DeepSide side, double price,
			int size) {
		System.out.println("Row: " + position + "\t" + "Market Maker: " + marketMaker + "\t" + "Operation: " + operation
				+ "\t" + "Side: " + side + "\t" + "Price: " + price + "\t" + "Size: " + size);
		/*
		 * switch (operation) { case INSERT: m_rows.add(position, new
		 * DeepRow(marketMaker, price, size)); break; case UPDATE:
		 * m_rows.get(position).update(marketMaker, price, size); break; case
		 * DELETE: if (position < m_rows.size()) { m_rows.remove(position); }
		 * break; }
		 */
	}
}
