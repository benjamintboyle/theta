package theta.connection.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import theta.properties.ThetaProperties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultConnectionAddressTest {

    @Mock
    private ThetaProperties mockThetaProperties;
    @Mock
    private ThetaProperties.Broker mockBrokerProperties;

    private DefaultConnectionAddress sut;

    @BeforeEach
    void setup() {
        sut = new DefaultConnectionAddress(mockThetaProperties, mockBrokerProperties);
    }

    @Test
    void getHostAddress_localhost() {
        assertThat(sut.getHostAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void getPort_localhost() {
        assertThat(sut.getPort()).isEqualTo(7497);
    }
}
