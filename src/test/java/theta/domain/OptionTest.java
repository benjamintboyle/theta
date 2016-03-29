package theta.domain;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.domain.Option;
import theta.domain.api.SecurityType;

@RunWith(MockitoJUnitRunner.class)
public class OptionTest {

	private static final LocalDate expiration = LocalDate.now().plusDays(30);

	public static Option buildTestCallOption() {
		return new Option(UUID.randomUUID(), SecurityType.CALL, "CHK", 1, 15.0, OptionTest.expiration);
	}

	public static Option buildTestShortCallOption() {
		return new Option(UUID.randomUUID(), SecurityType.CALL, "CHK", -1, 15.0, OptionTest.expiration);
	}

	public static Option buildTestPutOption() {
		return new Option(UUID.randomUUID(), SecurityType.PUT, "CHK", 1, 15.0, OptionTest.expiration);
	}

	public static Option buildTestShortPutOption() {
		return new Option(UUID.randomUUID(), SecurityType.PUT, "CHK", -1, 15.0, OptionTest.expiration);
	}

	@Test
	public void quantityCallTest() {
		Option call = OptionTest.buildTestCallOption();

		assertThat(call.getQuantity(), is(equalTo(1)));
	}

	@Test
	public void quantityShortCallTest() {
		Option shortCall = OptionTest.buildTestShortCallOption();

		assertThat(shortCall.getQuantity(), is(equalTo(-1)));
	}

	@Test
	public void quantityPutTest() {
		Option put = OptionTest.buildTestPutOption();

		assertThat(put.getQuantity(), is(equalTo(1)));
	}

	@Test
	public void quantityShortPutTest() {
		Option shortPut = OptionTest.buildTestShortPutOption();

		assertThat(shortPut.getQuantity(), is(equalTo(-1)));
	}

	@Test
	public void strikeCallTest() {
		Option call = OptionTest.buildTestCallOption();

		assertThat(call.getStrikePrice(), is(equalTo(15.0)));
	}
}
