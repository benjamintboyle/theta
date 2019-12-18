package brokers.interactivebrokers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class IbTickUtilTest {

  @Test
  public void testConvertToEngineTickTypeAsk() {

    final theta.tick.domain.TickType thetaTickType =
        IbTickUtil.convertToEngineTickType(com.ib.client.TickType.ASK);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.ASK));
  }

  @Test
  public void testConvertToEngineTickTypeBid() {

    final theta.tick.domain.TickType thetaTickType =
        IbTickUtil.convertToEngineTickType(com.ib.client.TickType.BID);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.BID));
  }

  @Test
  public void testConvertToEngineTickTypeLast() {

    final theta.tick.domain.TickType thetaTickType =
        IbTickUtil.convertToEngineTickType(com.ib.client.TickType.LAST);

    assertThat(thetaTickType, is(theta.tick.domain.TickType.LAST));
  }
}
