package brokers.interactive_brokers.tick.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import theta.api.TickHandler;
import theta.domain.Ticker;
import theta.domain.ticker.DefaultTicker;
import theta.tick.api.TickProcessor;
import theta.tick.processor.LastTickProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIbTickHandlerFactoryTest {

    private static final Ticker ticker = DefaultTicker.from("ABC");
    private static final TickProcessor tickProcessor = new LastTickProcessor();

    private DefaultIbTickHandlerFactory sut;

    @BeforeEach
    void setup() {
        sut = new DefaultIbTickHandlerFactory();
    }

    @Test
    void createTickHandler() {
        assertThat(sut.createTickHandler(ticker, tickProcessor)).isInstanceOf(TickHandler.class);
    }
}
