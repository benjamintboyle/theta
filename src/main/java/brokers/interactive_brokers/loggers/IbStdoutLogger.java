package brokers.interactive_brokers.loggers;

import com.ib.controller.ApiConnection.ILogger;

public class IbStdoutLogger implements ILogger {

	@Override
	public void log(String valueOf) {
		System.out.println(valueOf);
	}
}
