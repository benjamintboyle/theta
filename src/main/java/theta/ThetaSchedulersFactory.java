package theta;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThetaSchedulersFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ThetaSchedulersFactory() {

  }

  public static Scheduler ioThread() {
    logger.info("Creating Asynchronous Waiting Thread...");
    return Schedulers.io();
  }

  public static Scheduler computeThread() {
    logger.info("Acquiring next available Processing Thread...");
    return Schedulers.computation();
  }
}
