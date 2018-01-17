package theta;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ThetaSchedulersFactory {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static Executor unlimittedExecutor = Executors.newCachedThreadPool();

  public static Scheduler managerThread() {
    logger.info("Creating Manager Thread...");
    return Schedulers.io();
  }

  public static Scheduler asyncUnlimittedThread() {
    logger.info("Creating Asyncronous Waiting Thread...");

    return Schedulers.from(unlimittedExecutor);
  }

  public static Scheduler asyncFixedThread() {
    logger.info("Aquiring next available Processing Thread...");
    return Schedulers.computation();
  }
}
