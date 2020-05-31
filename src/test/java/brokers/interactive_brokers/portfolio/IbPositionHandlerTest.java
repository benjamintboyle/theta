package brokers.interactive_brokers.portfolio;

import brokers.interactive_brokers.IbController;
import com.ib.contracts.OptContract;
import com.ib.contracts.StkContract;
import com.ib.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import theta.domain.Security;
import theta.domain.SecurityType;
import theta.domain.option.Option;
import theta.domain.stock.Stock;
import theta.domain.ticker.DefaultTicker;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbPositionHandlerTest {

    @Mock
    private IbController mockController;
    @Mock
    private ApiController mockApiController;

    private IbPositionHandler sut;

    @BeforeEach
    void setup() {
        sut = new IbPositionHandler(mockController);
        when(mockController.getController()).thenReturn(mockApiController);
    }

    @Test
    void requestPositionsFromBrokerage() {
        Flux<Security> securitiesFlux = sut.requestPositionsFromBrokerage();
        sut.positionEnd();

        verify(mockApiController).reqPositions(isA(ApiController.IPositionHandler.class));
        StepVerifier.create(securitiesFlux).expectComplete().verify();
    }

    @Test
    void position() {
        Flux<Security> securitiesFlux = sut.requestPositionsFromBrokerage();

        String account = "TEST_ACCOUNT";
        String stockTickerSymbol = "ABC";
        double stockPosition = 100.0;
        double stockAverageCost = 123.1;

        sut.position(account, new StkContract(stockTickerSymbol), stockPosition, stockAverageCost);

        String optionTickerSymbol = "XYZ";
        LocalDate optionDate = LocalDate.of(2020, Month.MAY, 29);
        double optionStrike = 98.0;
        double optionPosition = 1.0;
        double optionAverageCost = 2.3;

        sut.position(account,
                new OptContract(optionTickerSymbol,
                        optionDate.format(DateTimeFormatter.BASIC_ISO_DATE),
                        optionStrike,
                        "Call"),
                optionPosition,
                optionAverageCost);

        sut.positionEnd();

        Stock expectStock = Stock.of(DefaultTicker.from(stockTickerSymbol),
                Double.valueOf(stockPosition).longValue(),
                stockAverageCost);
        Option expectOption = new Option(UUID.randomUUID(),
                SecurityType.CALL,
                DefaultTicker.from(optionTickerSymbol), Double.valueOf(optionPosition).longValue(), optionStrike,
                optionDate,
                optionAverageCost);

        StepVerifier.create(securitiesFlux)
                .expectNext(expectStock, expectOption)
                .expectComplete()
                .verify();
    }

    @Test
    void positionEnd() {
        Flux<Security> securitiesFlux = sut.requestPositionsFromBrokerage();
        sut.positionEnd();

        StepVerifier.create(securitiesFlux).expectComplete().verify();
    }

    @Test
    void shutdown() {
        Flux<Security> securitiesFlux = sut.requestPositionsFromBrokerage();
        // sut.shutdown();

        StepVerifier.create(securitiesFlux)
                .expectSubscription()
                .then(() -> sut.shutdown())
                .expectComplete()
                .verify();
    }
}
