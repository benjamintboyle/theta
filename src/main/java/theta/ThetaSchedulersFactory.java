package theta;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ThetaSchedulersFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Scheduler managerThread() {
    logger.info("Creating Manager Thread...");
    return Schedulers.io();
  }

  public static Scheduler asyncUnlimittedThread() {
    logger.info("Creating Asynchronous Waiting Thread...");
    return Schedulers.io();
  }

  public static Scheduler asyncFixedThread() {
    logger.info("Acquiring next available Processing Thread...");
    return Schedulers.computation();
  }
}
