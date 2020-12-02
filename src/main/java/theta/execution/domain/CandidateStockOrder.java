package theta.execution.domain;

import theta.domain.stock.Stock;
import theta.execution.api.ExecutionType;

import java.util.Optional;

public record CandidateStockOrder(Stock stock, ExecutionType executionType, Optional<Double> limitPrice) {
}
