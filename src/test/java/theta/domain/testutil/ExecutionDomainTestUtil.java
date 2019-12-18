package theta.domain.testutil;

import theta.domain.stock.Stock;
import theta.execution.api.ExecutableOrder;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.execution.domain.DefaultStockOrder;

public class ExecutionDomainTestUtil {

  private static Stock standardStock = ThetaDomainTestUtil.buildTestStock();
  private static double standardLimitPrice = 123.5;
  private static int standardBrokerId = 1234;


  // Build Test DefaultStockOrder "Buy Limit"
  public static DefaultStockOrder buildTestDefaultStockOrderNewBuyLimit() {
    return new DefaultStockOrder(standardStock, -2L * standardStock.getQuantity(),
        ExecutionAction.BUY, ExecutionType.LIMIT, standardLimitPrice);
  }

  public static ExecutableOrder buildTestExecutableOrderNewBuyLimit() {
    return buildTestDefaultStockOrderNewBuyLimit();
  }

  /**
   * Utility method to build buy Order that has been modified from original (not a new order).
   *
   * @return Modified buy order
   */
  public static DefaultStockOrder buildTestDefaultStockOrderModifiedBuyLimit() {

    final DefaultStockOrder order = buildTestDefaultStockOrderNewBuyLimit();
    order.setBrokerId(standardBrokerId);

    return order;
  }

  public static ExecutableOrder buildTestExecutableOrderModifiedBuyLimit() {
    return buildTestDefaultStockOrderModifiedBuyLimit();
  }


  // Build Test DefaultStockOrder "Sell Limit"
  public static DefaultStockOrder buildTestDefaultStockOrderNewSellLimit() {
    return new DefaultStockOrder(standardStock, -2L * standardStock.getQuantity(),
        ExecutionAction.SELL, ExecutionType.LIMIT, standardLimitPrice);
  }

  public static ExecutableOrder buildTestExecutableOrderNewSellLimit() {
    return buildTestDefaultStockOrderNewSellLimit();
  }

  /**
   * Utility method to build sell Order that has been modified from original (not a new order).
   *
   * @return Modified sell order
   */
  public static DefaultStockOrder buildTestDefaultStockOrderModifiedSellLimit() {

    final DefaultStockOrder order = buildTestDefaultStockOrderNewSellLimit();
    order.setBrokerId(standardBrokerId);

    return order;
  }

  public static ExecutableOrder buildTestExecutableOrderModifiedSellLimit() {
    return buildTestDefaultStockOrderModifiedSellLimit();
  }


  // Build Test DefaultStockOrder "Buy Market"
  public static DefaultStockOrder buildTestDefaultStockOrderNewBuyMarket() {
    return new DefaultStockOrder(standardStock, -2L * standardStock.getQuantity(),
        ExecutionAction.BUY, ExecutionType.MARKET);
  }

  public static ExecutableOrder buildTestExecutableOrderNewBuyMarket() {
    return buildTestDefaultStockOrderNewBuyMarket();
  }

  /**
   * Utility method to build buy Order that has been modified from original (not a new order).
   *
   * @return Modified buy order
   */
  public static DefaultStockOrder buildTestDefaultStockOrderModifiedBuyMarket() {

    final DefaultStockOrder order = buildTestDefaultStockOrderNewBuyMarket();
    order.setBrokerId(standardBrokerId);

    return order;
  }

  public static ExecutableOrder buildTestExecutableOrderModifiedBuyMarket() {
    return buildTestDefaultStockOrderModifiedBuyMarket();
  }


  // Build Test DefaultStockOrder "Sell Market"
  public static DefaultStockOrder buildTestDefaultStockOrderNewSellMarket() {
    return new DefaultStockOrder(standardStock, -2L * standardStock.getQuantity(),
        ExecutionAction.SELL, ExecutionType.MARKET);
  }

  public static ExecutableOrder buildTestExecutableOrderNewSellMarket() {
    return buildTestDefaultStockOrderNewSellMarket();
  }

  /**
   * Utility method to build sell Order that has been modified from original (not a new order).
   *
   * @return Modified sell order
   */
  public static DefaultStockOrder buildTestDefaultStockOrderModifiedSellMarket() {

    final DefaultStockOrder order = buildTestDefaultStockOrderNewSellMarket();
    order.setBrokerId(standardBrokerId);

    return order;
  }

  public static ExecutableOrder buildTestExecutableOrderModifiedSellMarket() {
    return buildTestDefaultStockOrderModifiedSellMarket();
  }

}
