package brokers.interactivebrokers;

import com.ib.controller.ApiConnection.ILogger;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IbLogger implements ILogger {

  private final String loggerName;

  public IbLogger(String loggerName) {
    this.loggerName = loggerName;
  }

  @Override
  public void log(String valueOf) {

    Optional.ofNullable(valueOf).map(String::trim).filter(Predicate.not(String::isEmpty)).ifPresent(

        stringValue -> log.info("{}: '{}'", loggerName, stringValue));
  }

}
