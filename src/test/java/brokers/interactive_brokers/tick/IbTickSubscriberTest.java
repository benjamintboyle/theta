package brokers.interactive_brokers.tick;

import brokers.interactive_brokers.IbController;
import brokers.interactive_brokers.tick.handler.IbTickHandler;
import brokers.interactive_brokers.tick.handler.IbTickHandlerFactory;
import com.ib.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import theta.domain.PriceLevel;
import theta.domain.Ticker;
import theta.domain.ticker.DefaultTicker;
import theta.tick.api.Tick;
import theta.tick.api.TickProcessor;
import theta.tick.domain.DefaultTick;
import theta.tick.domain.TickType;

import java.time.*;
import java.util.Set;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbTickSubscriberTest {
    @Mock
    private IbController mockIbController;
    @Mock
    private IbTickHandlerFactory mockTickHandlerFactory;
    @Mock
    private IbTickHandler mockIbTickHandler;
    @Mock
    private ApiController mockApiController;
    @Mock
    private PriceLevel mockPriceLevel;
    @Mock
    private TickProcessor mockTickProcessor;

    private IbTickSubscriber sut;

    @BeforeEach
    void setup() {
        sut = new IbTickSubscriber(mockIbController, mockTickHandlerFactory);
    }

    @Test
    void getTicksAcrossStrikePrices() {
        when(mockPriceLevel.getTicker()).thenReturn(DefaultTicker.from("XYZ"));
        when(mockIbController.getController()).thenReturn(mockApiController);

        final TestPublisher<Tick> testPublisher = TestPublisher.create();
        when(mockIbTickHandler.getTicks()).thenReturn(testPublisher.flux());
        when(mockIbTickHandler.getPriceLevelsMonitored()).thenReturn(Set.of(mockPriceLevel));
        when(mockTickHandlerFactory.createTickHandler(isA(Ticker.class), isA(TickProcessor.class)))
                .thenReturn(mockIbTickHandler);

        Tick firstTick = new DefaultTick(
                DefaultTicker.from("XYZ"),
                TickType.LAST,
                1.0,
                2.0,
                3.0,
                ZonedDateTime.of(
                        LocalDate.of(2011, Month.OCTOBER, 1),
                        LocalTime.of(11, 11, 11),
                        ZoneOffset.UTC)
                        .toInstant());

        sut.addPriceLevelMonitor(mockPriceLevel, mockTickProcessor);

        StepVerifier.create(sut.getTicksAcrossStrikePrices())
                .then(() -> testPublisher.emit(firstTick))
                .expectNext(firstTick)
                .then(() -> sut.unsubscribeAll())
                .expectComplete()
                .verify(Duration.ofSeconds(1L));
    }

    @Test
    void addPriceLevelMonitor() {
        when(mockPriceLevel.getTicker()).thenReturn(DefaultTicker.from("XYZ"));
        when(mockIbController.getController()).thenReturn(mockApiController);
        when(mockTickHandlerFactory.createTickHandler(isA(Ticker.class), isA(TickProcessor.class)))
                .thenReturn(mockIbTickHandler);
        final TestPublisher<Tick> testPublisher = TestPublisher.create();
        when(mockIbTickHandler.getTicks()).thenReturn(testPublisher.flux());
        when(mockIbTickHandler.getPriceLevelsMonitored()).thenReturn(Set.of(mockPriceLevel));

        sut.addPriceLevelMonitor(mockPriceLevel, mockTickProcessor);

        verify(mockIbTickHandler).addPriceLevelMonitor(mockPriceLevel);
    }

    @Test
    void removePriceLevelMonitor() {
        when(mockPriceLevel.getTicker()).thenReturn(DefaultTicker.from("XYZ"));
        when(mockIbController.getController()).thenReturn(mockApiController);
        when(mockTickHandlerFactory.createTickHandler(isA(Ticker.class), isA(TickProcessor.class)))
                .thenReturn(mockIbTickHandler);
        final TestPublisher<Tick> testPublisher = TestPublisher.create();
        when(mockIbTickHandler.getTicks()).thenReturn(testPublisher.flux());
        when(mockIbTickHandler.getPriceLevelsMonitored()).thenReturn(Set.of(mockPriceLevel));

        sut.addPriceLevelMonitor(mockPriceLevel, mockTickProcessor);
        sut.removePriceLevelMonitor(mockPriceLevel);

        verify(mockIbTickHandler).removePriceLevelMonitor(mockPriceLevel);
    }
}
