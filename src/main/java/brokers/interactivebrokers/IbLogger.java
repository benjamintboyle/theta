package brokers.interactivebrokers;

import com.ib.controller.ApiConnection.ILogger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IbLogger implements ILogger {

  private final String loggerName;

  public IbLogger(String loggerName) {
    this.loggerName = loggerName;
  }

  @Override
  public void log(String valueOf) {
    log.info("{}: {}", loggerName, valueOf);
  }
}
