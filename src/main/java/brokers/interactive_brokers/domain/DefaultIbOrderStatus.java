package brokers.interactive_brokers.domain;

import brokers.interactive_brokers.util.IbStringUtil;
import com.ib.client.OrderStatus;

public class DefaultIbOrderStatus implements IbOrderStatus {

    private final OrderStatus status;
    private final double filled;
    private final double remaining;
    private final double avgFillPrice;
    private final long permId;
    private final int parentId;
    private final double lastFillPrice;
    private final int clientId;
    private final String whyHeld;

    private DefaultIbOrderStatus(DefaultIbOrderStatusBuilder builder) {
        status = builder.status;
        filled = builder.filled;
        remaining = builder.remaining;
        avgFillPrice = builder.avgFillPrice;
        permId = builder.permId;
        parentId = builder.parentId;
        lastFillPrice = builder.lastFillPrice;
        clientId = builder.clientId;
        whyHeld = builder.whyHeld;
    }

    @Override
    public String toString() {
        return "Order Status: [" +
                "Status: " + status + IbStringUtil.DELIMITTER +
                "Filled: " + filled + IbStringUtil.DELIMITTER +
                "Remaining: " + remaining + IbStringUtil.DELIMITTER +
                "Avg Price: " + avgFillPrice + IbStringUtil.DELIMITTER +
                "Perm Id: " + permId + IbStringUtil.DELIMITTER +
                "Parent Id: " + parentId + IbStringUtil.DELIMITTER +
                "Last Fill Price: " + lastFillPrice + IbStringUtil.DELIMITTER +
                "Client Id: " + clientId + IbStringUtil.DELIMITTER +
                "Why Held: " + whyHeld + "]";
    }


    public static class DefaultIbOrderStatusBuilder implements IbOrderStatusBuilder {

        private OrderStatus status;
        private double filled;
        private double remaining;
        private double avgFillPrice;
        private long permId;
        private int parentId;
        private double lastFillPrice;
        private int clientId;
        private String whyHeld = null;

        public DefaultIbOrderStatusBuilder(OrderStatus status) {
            this.status = status;
        }

        @Override
        public IbOrderStatusBuilder numberFilled(double filledBuilder) {
            filled = filledBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder numberRemaining(double remainingBuilder) {
            remaining = remainingBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withAverageFillPrice(double avgFillPriceBuilder) {
            avgFillPrice = avgFillPriceBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withPermId(long permIdBuilder) {
            permId = permIdBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withParentId(int parentIdBuilder) {
            parentId = parentIdBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withLastFillPrice(double lastFillPriceBuilder) {
            lastFillPrice = lastFillPriceBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withClientId(int clientIdBuilder) {
            clientId = clientIdBuilder;
            return this;
        }

        @Override
        public IbOrderStatusBuilder withHeldReason(String whyHeldBuilder) {
            whyHeld = whyHeldBuilder;
            return this;
        }

        @Override
        public IbOrderStatus build() {
            return new DefaultIbOrderStatus(this);
        }
    }
}
