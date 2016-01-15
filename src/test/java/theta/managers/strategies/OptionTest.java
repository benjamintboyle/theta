package theta.managers.strategies;

import java.time.LocalDate;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import theta.strategies.Option;
import theta.strategies.api.SecurityType;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class OptionTest {

	private static final LocalDate expiration = LocalDate.now().plusDays(30);

	public static Option buildTestCallOption() {
		return new Option(SecurityType.CALL, "CHK", 1, 15.0, OptionTest.expiration);
	}

	public static Option buildTestPutOption() {
		return new Option(SecurityType.PUT, "CHK", 1, 15.0, OptionTest.expiration);
	}
}
