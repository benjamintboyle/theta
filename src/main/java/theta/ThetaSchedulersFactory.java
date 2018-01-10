package theta;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ThetaSchedulersFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Scheduler getManagerThread() {
    logger.info("Creating Manager Thread...");
    return Schedulers.io();
  }

  public static Scheduler getAsyncWaitThread() {
    logger.info("Creating Asyncronous Waiting Thread...");
    return Schedulers.io();
  }

  public static Scheduler getProcessingThread() {
    logger.info("Aquiring next available Processing Thread...");
    return Schedulers.computation();
  }
}
