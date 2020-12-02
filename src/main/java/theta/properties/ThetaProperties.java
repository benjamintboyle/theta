package theta.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("theta")
public class ThetaProperties {
    private final String ipAddress;

    public ThetaProperties(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public static class Broker {
        private final String ipAddress;

        private final int port;

        public Broker(String ipAddress, int port) {
            this.ipAddress = ipAddress;
            this.port = port;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getPort() {
            return port;
        }
    }
}
