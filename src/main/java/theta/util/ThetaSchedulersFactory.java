package theta.util;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ThetaSchedulersFactory {

    private ThetaSchedulersFactory() {
    }

    public static Scheduler ioThread() {
        return Schedulers.boundedElastic();
    }

    public static Scheduler computeThread() {
        return Schedulers.parallel();
    }
}
