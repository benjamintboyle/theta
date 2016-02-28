package theta.portfolio.api;

import java.util.List;
import java.util.Set;

import theta.domain.ThetaTrade;

public interface PositionProvider {
	public List<ThetaTrade> providePositions(Set<String> tickers);
}
