package brokers.interactive_brokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class IbTickUtilTest {

  @Test
  void testConvertToEngineTickTypeAsk() {

    theta.tick.domain.TickType thetaTickType = IbTickUtil.convertToEngineTickType(com.ib.client.TickType.ASK);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.ASK));
  }

  @Test
  void testConvertToEngineTickTypeBid() {

    theta.tick.domain.TickType thetaTickType = IbTickUtil.convertToEngineTickType(com.ib.client.TickType.BID);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.BID));
  }

  @Test
  void testConvertToEngineTickTypeLast() {

    theta.tick.domain.TickType thetaTickType = IbTickUtil.convertToEngineTickType(com.ib.client.TickType.LAST);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.LAST));
  }
}
