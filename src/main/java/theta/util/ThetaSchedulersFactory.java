package theta;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThetaSchedulersFactory {

  private ThetaSchedulersFactory() {

  }

  public static Scheduler ioThread() {
    log.info("Creating Asynchronous Waiting Thread...");
    return Schedulers.io();
  }

  public static Scheduler computeThread() {
    log.info("Acquiring next available Processing Thread...");
    return Schedulers.computation();
  }

}
