package brokers.interactive_brokers.domain;

import com.ib.client.OrderStatus;

public interface IbOrderStatusBuilder {

  IbOrderStatusBuilder withStatus(OrderStatus status);

  IbOrderStatusBuilder numberFilled(double filledBuilder);

  IbOrderStatusBuilder numberRemaining(double remainingBuilder);

  IbOrderStatusBuilder withAverageFillPrice(double avgFillPriceBuilder);

  IbOrderStatusBuilder withPermId(long permIdBuilder);

  IbOrderStatusBuilder withParentId(int parentIdBuilder);

  IbOrderStatusBuilder withLastFillPrice(double lastFillPriceBuilder);

  IbOrderStatusBuilder withClientId(int clientIdBuilder);

  IbOrderStatusBuilder withHeldReason(String whyHeldBuilder);

  IbOrderStatus build();
}
