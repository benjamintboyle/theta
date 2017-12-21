package theta;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import theta.api.ExecutionHandler;
import theta.api.PositionHandler;
import theta.api.TickSubscriber;
import theta.domain.ThetaTrade;
import theta.domain.ThetaTradeTest;
import theta.domain.api.Security;
import theta.execution.api.ExecutableOrder;
import theta.execution.manager.ExecutionManager;
import theta.portfolio.manager.PortfolioManager;
import theta.tick.domain.Tick;
import theta.tick.domain.TickType;
import theta.tick.manager.TickManager;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IntegrationTest {

  @Mock
  private ExecutionHandler mockExecutionHandler;
  @Mock
  private PositionHandler mockPositionHandler;
  @Mock
  private TickSubscriber mockTickSubscriber;

  private ExecutionManager spyExecutionManager;
  private PortfolioManager spyPortfolioManager;
  private TickManager spyTickManager = null;

  private List<Tick> generateStandardTickPatternList(final List<Tick> zeroTicks) {
    final List<Tick> tickList = new ArrayList<Tick>();
    final TickType tickType = TickType.LAST;

    for (final Tick tick : zeroTicks) {
      final String ticker = tick.getTicker();
      final Double strike = tick.getPrice();

      tickList.add(new Tick(ticker, strike, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike + 0.01, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike + 0.01, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike - 0.01, tickType, ZonedDateTime.now()));
      tickList.add(new Tick(ticker, strike - 0.01, tickType, ZonedDateTime.now()));
      /*
       * tickList.add(new Tick(ticker, strike, tickType, LocalDateTime.now())); tickList.add(new
       * Tick(ticker, strike, tickType, LocalDateTime.now())); tickList.add(new Tick(ticker, strike
       * + 0.01, tickType, LocalDateTime.now())); tickList.add(new Tick(ticker, strike + 0.01,
       * tickType, LocalDateTime.now())); tickList.add(new Tick(ticker, strike - 0.01, tickType,
       * LocalDateTime.now())); tickList.add(new Tick(ticker, strike - 0.01, tickType,
       * LocalDateTime.now())); tickList.add(new Tick(ticker, strike + 0.01, tickType,
       * LocalDateTime.now())); tickList.add(new Tick(ticker, strike + 0.01, tickType,
       * LocalDateTime.now())); tickList.add(new Tick(ticker, strike - 0.01, tickType,
       * LocalDateTime.now())); tickList.add(new Tick(ticker, strike + 0.01, tickType,
       * LocalDateTime.now()));
       */
    }

    return tickList;
  }

  private List<Tick> generateTickList(final List<ThetaTrade> thetaList) {
    final List<Tick> returnTicks = new ArrayList<Tick>();
    final List<Tick> zeroTicks = new ArrayList<Tick>();

    zeroTicks.addAll(generateZeroTickList(thetaList));
    returnTicks.addAll(generateStandardTickPatternList(zeroTicks));

    return returnTicks;
  }

  private List<Tick> generateZeroTickList(final List<ThetaTrade> thetaList) {
    final List<Tick> zeroTickList = new ArrayList<Tick>();

    for (final ThetaTrade theta : thetaList) {
      zeroTickList.add(
          new Tick(theta.getTicker(), theta.getStrikePrice(), TickType.LAST, ZonedDateTime.now()));
    }
    return zeroTickList;
  }

  @Ignore
  @Test
  public void IntegrationTest_OnePositionOneContract() {
    final ThetaTrade thetaTrade = ThetaTradeTest.buildTestThetaTrade();

    // Add all securities to be ingested to a list
    final List<Security> listOfSecurities =
        List.of(thetaTrade.getStock(), thetaTrade.getCall(), thetaTrade.getPut());

    // Send all securities in list for ingestion
    sendPositionListForIngestion(listOfSecurities);

    final List<ThetaTrade> thetaList = new ArrayList<ThetaTrade>();
    thetaList.add(thetaTrade);
    final List<Tick> tickList = generateTickList(thetaList);

    sendTickListForIngestion(tickList);

    Mockito.verify(mockExecutionHandler, Mockito.times(6))
        .executeStockEquityMarketOrder(ArgumentMatchers.any(ExecutableOrder.class));
  }

  @Ignore
  @Test
  public void integrationTest_onePositionThreeContracts() {
    // assertThat();
    Assert.fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void integrationTest_onePositionThreeStrikePrices() {
    // assertThat();
    Assert.fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void integrationTest_threePositionsOneContract() {
    // assertThat();
    Assert.fail("Not yet implemented");
  }

  @Ignore
  @Test
  public void integrationTest_threePositionsThreeContracts() {
    // assertThat();
    Assert.fail("Not yet implemented");
  }

  private void sendPositionListForIngestion(final List<Security> positions) {
    positions.forEach(spyPortfolioManager::acceptPosition);
  }

  private void sendTickListForIngestion(final List<Tick> tickList) {
    tickList.stream().forEach(tick -> spyTickManager.acceptTick(tick.getTicker()));
  }

  @Before
  public void setUp() {
    spyExecutionManager = Mockito.spy(new ExecutionManager(mockExecutionHandler));
    spyTickManager = Mockito.spy(new TickManager(mockTickSubscriber));
    spyPortfolioManager = Mockito.spy(new PortfolioManager(mockPositionHandler));

    spyPortfolioManager.registerTickMonitor(spyTickManager);
    spyTickManager.registerExecutor(spyExecutionManager);
    spyTickManager.registerPositionProvider(spyPortfolioManager);
  }
}
