package brokers.interactive_brokers.domain;

import java.util.Objects;
import com.ib.client.OrderStatus;
import brokers.interactive_brokers.util.IbStringUtil;

public class IbOrderStatus {

  private OrderStatus status = null;
  private final double filled;
  private final double remaining;
  private final double avgFillPrice;
  private final long permId;
  private final int parentId;
  private final double lastFillPrice;
  private final int clientId;
  private String whyHeld = null;

  private IbOrderStatus(IbOrderStatusBuilder builder) {
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

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("Order Status: [");

    stringBuilder.append("Status: ");
    stringBuilder.append(Objects.toString(status));

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Filled: ");
    stringBuilder.append(filled);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Remaining: ");
    stringBuilder.append(remaining);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Avg Price: ");
    stringBuilder.append(avgFillPrice);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Perm Id: ");
    stringBuilder.append(permId);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Parent Id: ");
    stringBuilder.append(parentId);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Last Fill Price: ");
    stringBuilder.append(lastFillPrice);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Client Id: ");
    stringBuilder.append(clientId);

    stringBuilder.append(IbStringUtil.DELIMITTER);
    stringBuilder.append("Why Held: ");
    stringBuilder.append(Objects.toString(whyHeld));

    stringBuilder.append("]");

    return stringBuilder.toString();
  }


  public static class IbOrderStatusBuilder {

    private OrderStatus status = null;
    private double filled;
    private double remaining;
    private double avgFillPrice;
    private long permId;
    private int parentId;
    private double lastFillPrice;
    private int clientId;
    private String whyHeld = null;

    public IbOrderStatusBuilder(OrderStatus status) {
      this.status = status;
    }

    public IbOrderStatusBuilder numberFilled(double filled) {
      this.filled = filled;
      return this;
    }

    public IbOrderStatusBuilder numberRemaining(double remaining) {
      this.remaining = remaining;
      return this;
    }

    public IbOrderStatusBuilder withAverageFillPrice(double avgFillPrice) {
      this.avgFillPrice = avgFillPrice;
      return this;
    }

    public IbOrderStatusBuilder withPermId(long permId) {
      this.permId = permId;
      return this;
    }

    public IbOrderStatusBuilder withParentId(int parentId) {
      this.parentId = parentId;
      return this;
    }

    public IbOrderStatusBuilder withLastFillPrice(double lastFillPrice) {
      this.lastFillPrice = lastFillPrice;
      return this;
    }

    public IbOrderStatusBuilder withClientId(int clientId) {
      this.clientId = clientId;
      return this;
    }

    public IbOrderStatusBuilder withHeldReason(String whyHeld) {
      this.whyHeld = whyHeld;
      return this;
    }

    public IbOrderStatus build() {
      return new IbOrderStatus(this);
    }
  }
}
