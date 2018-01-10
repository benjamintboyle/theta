package brokers.interactive_brokers;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.controller.ApiConnection.ILogger;

public class IbLogger implements ILogger {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String loggerName;

  public IbLogger(String loggerName) {
    this.loggerName = loggerName;
  }

  @Override
  public void log(String valueOf) {
    logger.info("{}: {}", loggerName, valueOf);
  }
}
