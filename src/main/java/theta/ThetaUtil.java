package theta;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThetaUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String NAME_PREFIX = "Thread-";

  public static void updateThreadName(String newName) {
    final String newNameWithSuffix = NAME_PREFIX + newName;
    final String oldName = Thread.currentThread().getName();

    if (!oldName.equals(newNameWithSuffix)) {
      logger.info("Renaming Thread: '{}' to '{}'", oldName, newNameWithSuffix);

      Thread.currentThread().setName(newNameWithSuffix);
    }
  }
}
