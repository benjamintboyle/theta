package brokers.interactive_brokers.tick.handler;

import org.springframework.stereotype.Component;
import theta.domain.Ticker;
import theta.tick.api.TickProcessor;

@Component
public class DefaultIbTickHandlerFactory implements IbTickHandlerFactory {
    @Override
    public IbTickHandler createTickHandler(Ticker ticker, TickProcessor tickProcessor) {
        return new IbTickHandler(ticker, tickProcessor);
    }
}
