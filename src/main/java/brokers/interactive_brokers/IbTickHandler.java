package brokers.interactive_brokers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewTickType;
import com.ib.controller.Types.MktDataType;

import theta.api.TickHandler;
import theta.tick.api.PriceLevel;
import theta.tick.api.TickObserver;

public class IbTickHandler implements ITopMktDataHandler, TickHandler {
	private static final Logger logger = LoggerFactory.getLogger(IbTickHandler.class);

	private TickObserver tickObserver;

	private String ticker;
	private Double bidPrice = Double.MIN_VALUE;
	private Double askPrice = Double.MIN_VALUE;
	private Double lastPrice = Double.MIN_VALUE;
	private Instant lastTime = Instant.now();
	private Integer bidSize = Integer.MIN_VALUE;
	private Integer askSize = Integer.MIN_VALUE;
	private Double closePrice = Double.MIN_VALUE;
	private Integer volume = Integer.MIN_VALUE;
	private Boolean isSnapshot;

	private Set<Double> fallsBelow = new HashSet<Double>();
	private Set<Double> risesAbove = new HashSet<Double>();

	public IbTickHandler(String ticker, TickObserver tickObserver) {
		this.ticker = ticker;
		this.tickObserver = tickObserver;
		logger.info("Built Interactive Brokers Tick Handler for: {}", ticker);
	}

	@Override
	public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {
		logger.info(
				"Received Tick from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}",
				this.ticker, tickType, price, canAutoExecute);

		switch (tickType) {
		case BID:
			this.bidPrice = price;
			break;
		case ASK:
			this.askPrice = price;
			break;
		case LAST:
			this.lastPrice = price;

			this.priceTrigger(this.lastPrice);

			break;
		case CLOSE:
			this.closePrice = price;
			break;
		default:
			logger.error("Tick not logged for: {}", tickType);
			break;
		}
	}

	@Override
	public void tickSize(NewTickType tickType, int size) {
		logger.info("Received Tick Size from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Size: {}",
				this.ticker, tickType, size);

		switch (tickType) {
		case BID_SIZE:
			this.bidSize = size;
			break;
		case ASK_SIZE:
			this.askSize = size;
			break;
		case VOLUME:
			this.volume = size;
			break;
		default:
			logger.error("Tick Size not logged for: {}", tickType);
			break;
		}
	}

	@Override
	public void tickString(NewTickType tickType, String value) {
		logger.info("Received Tick String from Interactive Brokers servers - Ticker: {}, Tick Type: {}, Value: {}",
				this.ticker, tickType, value);

		switch (tickType) {
		case LAST_TIMESTAMP:
			this.lastTime = Instant.ofEpochSecond(Long.parseLong(value));
			break;
		default:
			logger.error("Tick String not logged for: {}", tickType);
			break;
		}
	}

	@Override
	public void marketDataType(MktDataType marketDataType) {
		logger.info("Received Market Data from Interactive Brokers servers - Ticker: {}, Market Date Type: {}",
				this.ticker, marketDataType);

		this.isSnapshot = marketDataType == MktDataType.Frozen;
	}

	@Override
	public void tickSnapshotEnd() {
		logger.info("Ticker: {}, Tick Snapshot End", this.ticker);
	}

	private void priceTrigger(Double price) {
		for (Double priceToFallBelow : this.fallsBelow) {
			if (price < priceToFallBelow) {
				this.tickObserver.notifyTick(this.ticker);
			}
		}
		for (Double priceToRiseAbove : this.risesAbove) {
			if (price > priceToRiseAbove) {
				this.tickObserver.notifyTick(this.ticker);
			}
		}
	}

	@Override
	public String getTicker() {
		return this.ticker;
	}

	@Override
	public Double getBid() {
		return this.bidPrice;
	}

	@Override
	public Double getAsk() {
		return this.askPrice;
	}

	@Override
	public Double getLast() {
		return this.lastPrice;
	}

	@Override
	public LocalDateTime getLastTime() {
		return LocalDateTime.ofInstant(this.lastTime, ZoneOffset.UTC);
	}

	@Override
	public Integer getBidSize() {
		return this.bidSize;
	}

	@Override
	public Integer getAskSize() {
		return this.askSize;
	}

	@Override
	public Double getClose() {
		return this.closePrice;
	}

	@Override
	public Integer getVolume() {
		return this.volume;
	}

	@Override
	public Boolean isSnapshot() {
		return this.isSnapshot;
	}

	@Override
	public Integer addPriceLevel(PriceLevel priceLevel) {
		logger.info("Adding Price Level {} to Tick Handler: {}", priceLevel, this.ticker);
		if (!priceLevel.getTicker().equals(this.ticker)) {
			logger.error("Attempted to add PriceLevel for '{}' to '{}' Monitor", priceLevel.getTicker(), this.ticker);
		}

		switch (priceLevel.tradeIf()) {
		case FALLS_BELOW:
			logger.info("Adding FALLS_BELOW price level: {}", priceLevel);
			this.fallsBelow.add(priceLevel.getStrikePrice());
			break;
		case RISES_ABOVE:
			logger.info("Adding RISES_ABOVE price level: {}", priceLevel);
			this.risesAbove.add(priceLevel.getStrikePrice());
			break;
		default:
			logger.error("Unknown Price Direction: {}", priceLevel.tradeIf());
		}

		return this.fallsBelow.size() + this.risesAbove.size();
	}

	@Override
	public Integer removePriceLevel(PriceLevel priceLevel) {
		logger.info("Removing Price Level {} from Tick Handler: {}", priceLevel, this.ticker);
		if (!priceLevel.getTicker().equals(this.ticker)) {
			logger.error("Attempted to remove PriceLevel for '{}' from '{}' Monitor", priceLevel.getTicker(),
					this.ticker);
		}

		switch (priceLevel.tradeIf()) {
		case FALLS_BELOW:
			logger.info("Removing FALLS_BELOW price level: {}", priceLevel);
			this.fallsBelow.remove(priceLevel.getStrikePrice());
			break;
		case RISES_ABOVE:
			logger.info("Removing RISES_ABOVE price level: {}", priceLevel);
			this.risesAbove.remove(priceLevel.getStrikePrice());
			break;
		default:
			logger.error("Unknown Price Direction: {}", priceLevel.tradeIf());
		}

		return this.fallsBelow.size() + this.risesAbove.size();
	}
}
