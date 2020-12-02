package brokers.interactive_brokers.tick.handler;

import theta.domain.Ticker;
import theta.tick.api.TickProcessor;

public interface IbTickHandlerFactory {
    IbTickHandler createTickHandler(Ticker ticker, TickProcessor tickProcessor);
}
