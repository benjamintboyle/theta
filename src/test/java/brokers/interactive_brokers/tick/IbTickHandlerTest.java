package brokers.interactive_brokers.tick;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ib.client.TickType;
import io.reactivex.subscribers.TestSubscriber;
import theta.domain.DefaultPriceLevel;
import theta.domain.ThetaDomainFactory;
import theta.domain.Ticker;
import theta.domain.api.PriceLevel;
import theta.domain.api.PriceLevelDirection;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.util.ThetaMarketUtil;

@ExtendWith(MockitoExtension.class)
class IbTickHandlerTest {

  @Mock
  private Ticker mockTicker;

  @Mock
  private TickProcessor mockTickProcessor;

  private IbTickHandler sut;

  @BeforeEach
  void setup() {
    PriceLevel priceLevel = ThetaDomainFactory.buildDefaultPriceLevel();
    sut = new IbTickHandler(priceLevel.getTicker(), mockTickProcessor);
    sut.addPriceLevelMonitor(priceLevel);
  }

  @Test
  void testGetTicks() {

    final double BID_PRICE = 1234.0;

    // Setup for all mock calls
    when(mockTickProcessor.isApplicable(any(theta.tick.domain.TickType.class))).thenReturn(true);
    when(mockTickProcessor.processTick(any(Tick.class), any(PriceLevel.class))).thenReturn(true);

    // Inject ticks
    sut.tickPrice(TickType.BID, BID_PRICE, 0);

    final Tick tick = sut.getTicks().blockingFirst();

    assertThat(tick.getBidPrice(), is(equalTo(BID_PRICE)));
    assertThat(tick.getTicker(), is(equalTo(sut.getTicker())));
    assertThat(tick.getTickType(), is(equalTo(theta.tick.domain.TickType.BID)));
    assertThat(tick.getTimestamp(),
        is(not(equalTo(ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE)))));
  }

  @Test
  void testTickPrice() {

    final double ASK_PRICE = 999.0;
    final double LAST_PRICE = 777.0;
    final double BID_PRICE = 333.0;

    // Setup for all mock calls
    when(mockTickProcessor.isApplicable(any(theta.tick.domain.TickType.class))).thenReturn(true);
    when(mockTickProcessor.processTick(any(Tick.class), any(PriceLevel.class))).thenReturn(true);


    // Checking ASK ticks
    sut.tickPrice(TickType.ASK, ASK_PRICE, 0);

    final Tick askTick = sut.getTicks().blockingFirst();

    assertThat(askTick.getAskPrice(), is(equalTo(ASK_PRICE)));
    assertThat(askTick.getTicker(), is(equalTo(sut.getTicker())));
    assertThat(askTick.getTickType(), is(equalTo(theta.tick.domain.TickType.ASK)));
    assertThat(askTick.getTimestamp(),
        is(not(equalTo(ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE)))));


    // Checking LAST ticks
    sut.tickPrice(TickType.LAST, LAST_PRICE, 0);

    final Tick lastTick = sut.getTicks().blockingFirst();

    assertThat(lastTick.getLastPrice(), is(equalTo(LAST_PRICE)));
    assertThat(lastTick.getTicker(), is(equalTo(sut.getTicker())));
    assertThat(lastTick.getTickType(), is(equalTo(theta.tick.domain.TickType.LAST)));
    assertThat(lastTick.getTimestamp(),
        is(equalTo(ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE))));


    // Checking BID ticks
    sut.tickPrice(TickType.BID, BID_PRICE, 0);

    final Tick bidTick = sut.getTicks().blockingFirst();

    assertThat(bidTick.getBidPrice(), is(equalTo(BID_PRICE)));
    assertThat(bidTick.getTicker(), is(equalTo(sut.getTicker())));
    assertThat(bidTick.getTickType(), is(equalTo(theta.tick.domain.TickType.BID)));
    assertThat(bidTick.getTimestamp(),
        is(not(equalTo(ZonedDateTime.ofInstant(Instant.EPOCH, ThetaMarketUtil.MARKET_TIMEZONE)))));
  }

  @Test
  void testTickString() {

    // Setup for all mock calls
    when(mockTickProcessor.isApplicable(any(theta.tick.domain.TickType.class))).thenReturn(true);
    when(mockTickProcessor.processTick(any(Tick.class), any(PriceLevel.class))).thenReturn(true);

    // Inject ticks
    long epochSecondsNow = Instant.now().getEpochSecond();
    sut.tickString(TickType.LAST_TIMESTAMP, String.valueOf(epochSecondsNow));
    sut.tickPrice(TickType.LAST, 2.0, 0);

    Tick tick = sut.getTicks().blockingFirst();

    Instant expectedInstant = Instant.ofEpochSecond(epochSecondsNow);

    assertThat(tick.getTimestamp().toInstant(), is(equalTo(expectedInstant)));
  }

  @Test
  void testAddPriceLevelMonitor() {

    final Set<PriceLevel> priceLevelSet = sut.getPriceLevelsMonitored();

    assertThat(priceLevelSet.size(), is(equalTo(1)));

    final PriceLevel originalPriceLevel = priceLevelSet.stream().findFirst().get();

    final int numberOfPriceLevelsAfterDuplicateAdd =
        sut.addPriceLevelMonitor(ThetaDomainFactory.buildDefaultPriceLevel());

    assertThat(numberOfPriceLevelsAfterDuplicateAdd, is(equalTo(1)));

    PriceLevel additionalPriceLevel = DefaultPriceLevel.from(originalPriceLevel.getTicker(),
        originalPriceLevel.getPrice() + 1, PriceLevelDirection.FALLS_BELOW);

    final int numberOfPriceLevelsAfterNewAdd = sut.addPriceLevelMonitor(additionalPriceLevel);

    assertThat(numberOfPriceLevelsAfterNewAdd, is(equalTo(2)));
  }

  @Test
  void testRemovePriceLevelMonitor() {

    Set<PriceLevel> priceLevelSet = sut.getPriceLevelsMonitored();

    assertThat(priceLevelSet.size(), is(equalTo(1)));

    PriceLevel priceLevel = priceLevelSet.stream().findFirst().get();

    int numberOfPriceLevels = sut.removePriceLevelMonitor(priceLevel);

    assertThat(numberOfPriceLevels, is(equalTo(0)));

    Set<PriceLevel> priceLevelSetAfterRemoval = sut.getPriceLevelsMonitored();

    assertThat(priceLevelSetAfterRemoval.size(), is(equalTo(0)));
  }

  @Test
  void testGetPriceLevelsMonitored() {

    Set<PriceLevel> priceLevelSet = sut.getPriceLevelsMonitored();

    assertThat(priceLevelSet.size(), is(equalTo(1)));
  }

  @Test
  void testCancel() {

    final TestSubscriber<Tick> ticksTestSubscriber = sut.getTicks().test();

    ticksTestSubscriber.assertNotComplete().assertNotTerminated();

    sut.cancel();

    ticksTestSubscriber.assertComplete().assertTerminated();
  }

  @Test
  void testToString() {

    String toStringTickHandler = sut.toString();

    assertThat("toString() should not be empty.", toStringTickHandler, is(not(emptyString())));
    assertThat("Found '@' which likely indicates an unexpanded reference.", toStringTickHandler,
        not(containsString("@")));
  }

}
