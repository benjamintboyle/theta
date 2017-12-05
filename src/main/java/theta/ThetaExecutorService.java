package theta;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class ThetaExecutorService {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final ExecutorService MANAGER_THREAD_POOL = Executors.newCachedThreadPool();
  private static final ExecutorService PROCESSING_THREAD_POOL = Executors.newWorkStealingPool();

  public static Scheduler getManagerThread() {
    logger.info("Creating Manager Thread...");
    return Schedulers.from(MANAGER_THREAD_POOL);
  }

  public static Scheduler getAsyncWaitThread() {
    logger.info("Creating Asyncronous Waiting Thread...");
    return Schedulers.io();
  }

  public static Scheduler getProcessingThread() {
    logger.info("Aquiring next available Processing Thread...");
    return Schedulers.from(PROCESSING_THREAD_POOL);
  }
}
