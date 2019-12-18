package brokers.interactivebrokers;

import com.ib.controller.ApiConnection.ILogger;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IbLogger implements ILogger {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String loggerName;

  public IbLogger(String loggerName) {
    this.loggerName = loggerName;
  }

  @Override
  public void log(String valueOf) {
    logger.info("{}: {}", loggerName, valueOf);
  }
}
