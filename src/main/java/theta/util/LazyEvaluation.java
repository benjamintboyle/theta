package theta.util;

import java.util.function.Supplier;

public class LazyEvaluation {

  private LazyEvaluation() {}

  public static Supplier<Object> lazy(Supplier<Object> eagerSupplier) {
    return new LazyEvaluationWrapper(eagerSupplier);
  }

  private static class LazyEvaluationWrapper implements Supplier<Object> {

    private Supplier<Object> supplier = null;

    private LazyEvaluationWrapper(Supplier<Object> eagerSupplier) {
      supplier = eagerSupplier;
    }

    @Override
    public Object get() {
      return supplier.get();
    }
  }
}
