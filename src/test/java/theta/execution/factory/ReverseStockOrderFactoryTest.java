package theta.execution.factory;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import theta.domain.SecurityType;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.domain.CandidateStockOrder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReverseStockOrderFactoryTest {

    private static final Stock STOCK = Stock.of(DefaultTicker.from("ABC"), 100L, 123.4);
    private static final Stock SHORT_STOCK = Stock.of(DefaultTicker.from("ABC"), -100L, 123.4);

    @Test
    void reverse_marketOrder() {
        CandidateStockOrder candidateOrder = new CandidateStockOrder(STOCK, ExecutionType.MARKET, Optional.empty());
        ExecutableOrder reverseOrder = ReverseStockOrderFactory.reverse(candidateOrder);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reverseOrder.getId()).isNotNull();
            softly.assertThat(reverseOrder.getTicker()).isEqualTo(STOCK.getTicker());
            softly.assertThat(reverseOrder.getSecurityType()).isEqualTo(SecurityType.STOCK);
            softly.assertThat(reverseOrder.getExecutionAction()).isEqualTo(ExecutionAction.SELL);
            softly.assertThat(reverseOrder.getExecutionType()).isEqualTo(ExecutionType.MARKET);
            softly.assertThat(reverseOrder.getLimitPrice()).isEqualTo(Optional.empty());
            softly.assertThat(reverseOrder.getQuantity()).isEqualTo(200L);
            softly.assertThat(reverseOrder.getBrokerId()).isEqualTo(Optional.empty());
        });
    }

    @Test
    void reverseShort_marketOrder() {
        CandidateStockOrder candidateOrder = new CandidateStockOrder(SHORT_STOCK, ExecutionType.MARKET, Optional.empty());
        ExecutableOrder reverseOrder = ReverseStockOrderFactory.reverse(candidateOrder);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reverseOrder.getId()).isNotNull();
            softly.assertThat(reverseOrder.getTicker()).isEqualTo(STOCK.getTicker());
            softly.assertThat(reverseOrder.getSecurityType()).isEqualTo(SecurityType.STOCK);
            softly.assertThat(reverseOrder.getExecutionAction()).isEqualTo(ExecutionAction.BUY);
            softly.assertThat(reverseOrder.getExecutionType()).isEqualTo(ExecutionType.MARKET);
            softly.assertThat(reverseOrder.getLimitPrice()).isEqualTo(Optional.empty());
            softly.assertThat(reverseOrder.getQuantity()).isEqualTo(200L);
            softly.assertThat(reverseOrder.getBrokerId()).isEqualTo(Optional.empty());
        });
    }

    @Test
    void reverse_limitOrder() {
        CandidateStockOrder candidateOrder = new CandidateStockOrder(STOCK, ExecutionType.LIMIT, Optional.of(33.3));
        ExecutableOrder reverseOrder = ReverseStockOrderFactory.reverse(candidateOrder);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reverseOrder.getId()).isNotNull();
            softly.assertThat(reverseOrder.getTicker()).isEqualTo(STOCK.getTicker());
            softly.assertThat(reverseOrder.getSecurityType()).isEqualTo(SecurityType.STOCK);
            softly.assertThat(reverseOrder.getExecutionAction()).isEqualTo(ExecutionAction.SELL);
            softly.assertThat(reverseOrder.getExecutionType()).isEqualTo(ExecutionType.LIMIT);
            softly.assertThat(reverseOrder.getLimitPrice()).isEqualTo(Optional.of(33.3));
            softly.assertThat(reverseOrder.getQuantity()).isEqualTo(200L);
            softly.assertThat(reverseOrder.getBrokerId()).isEqualTo(Optional.empty());
        });
    }

    @Test
    void reverseShort_limitOrder() {
        CandidateStockOrder candidateOrder = new CandidateStockOrder(SHORT_STOCK, ExecutionType.LIMIT, Optional.of(33.3));
        ExecutableOrder reverseOrder = ReverseStockOrderFactory.reverse(candidateOrder);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reverseOrder.getId()).isNotNull();
            softly.assertThat(reverseOrder.getTicker()).isEqualTo(STOCK.getTicker());
            softly.assertThat(reverseOrder.getSecurityType()).isEqualTo(SecurityType.STOCK);
            softly.assertThat(reverseOrder.getExecutionAction()).isEqualTo(ExecutionAction.BUY);
            softly.assertThat(reverseOrder.getExecutionType()).isEqualTo(ExecutionType.LIMIT);
            softly.assertThat(reverseOrder.getLimitPrice()).isEqualTo(Optional.of(33.3));
            softly.assertThat(reverseOrder.getQuantity()).isEqualTo(200L);
            softly.assertThat(reverseOrder.getBrokerId()).isEqualTo(Optional.empty());
        });
    }

    @Test
    void reverse_limitOrder_exceptionLimitPrice() {
        CandidateStockOrder candidateOrder = new CandidateStockOrder(STOCK, ExecutionType.LIMIT, Optional.empty());

        assertThatThrownBy(() -> ReverseStockOrderFactory.reverse(candidateOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit price not set");
    }
}
