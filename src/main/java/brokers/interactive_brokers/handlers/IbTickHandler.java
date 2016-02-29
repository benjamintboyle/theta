package brokers.interactive_brokers.handlers;

import com.ib.controller.NewTickType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Types.MktDataType;

import theta.api.TickHandler;
import theta.tick.api.PriceLevel;

public class IbTickHandler implements ITopMktDataHandler, TickHandler {
	private final Logger logger = LoggerFactory.getLogger(IbTickHandler.class);

	private String ticker;
	private Double bidPrice;
	private Double askPrice;
	private Double lastPrice;
	private Long lastTime;
	private Integer bidSize;
	private Integer askSize;
	private Double closePrice;
	private Integer volume;
	private Boolean isSnapshot;

	private Set<Double> fallsBelow = new HashSet<Double>();
	private Set<Double> risesAbove = new HashSet<Double>();

	@Override
	public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {
		logger.info("Ticker: {}, Tick Type: {}, Price: {}, CanAutoExecute: {}", this.ticker, tickType, price,
				canAutoExecute);

		switch (tickType) {
		case BID:
			this.bidPrice = price;
			break;
		case ASK:
			this.askPrice = price;
			break;
		case LAST:
			this.lastPrice = price;
			break;
		case CLOSE:
			this.closePrice = price;
			break;
		default:
			break;
		}
	}

	@Override
	public void tickSize(NewTickType tickType, int size) {
		logger.info("Ticker: {}, Tick Type: {}, Size: {}", this.ticker, tickType, size);

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
			break;
		}
	}

	@Override
	public void tickString(NewTickType tickType, String value) {
		logger.info("Ticker: {}, Tick Type: {}, Value: {}", this.ticker, tickType, value);

		switch (tickType) {
		case LAST_TIMESTAMP:
			this.lastTime = Long.parseLong(value) * 1000;
			break;
		default:
			break;
		}
	}

	@Override
	public void marketDataType(MktDataType marketDataType) {
		logger.info("Ticker: {}, Market Date Type: {}", this.ticker, marketDataType);

		this.isSnapshot = marketDataType == MktDataType.Frozen;
	}

	@Override
	public void tickSnapshotEnd() {
		logger.info("Ticker: {}, Tick Snapshot End", this.ticker);
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
		return LocalDateTime.ofEpochSecond(this.lastTime, 0, ZoneOffset.UTC);
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
	public void addPriceLevel(PriceLevel priceLevel) {
		if (!priceLevel.getBackingTicker().equals(this.ticker)) {
			logger.error("Attempted to add PriceLevel for '{}' to '{}' Monitor", priceLevel.getBackingTicker(),
					this.ticker);
		}

		switch (priceLevel.tradeIf()) {
		case FALLS_BELOW:
			this.fallsBelow.add(priceLevel.getStrikePrice());
			break;
		case RISES_ABOVE:
			this.risesAbove.add(priceLevel.getStrikePrice());
			break;
		default:
			logger.error("Unknown Price Direction: {}", priceLevel.tradeIf());
		}
	}

	@Override
	public void removePriceLevel(PriceLevel priceLevel) {
		if (!priceLevel.getBackingTicker().equals(this.ticker)) {
			logger.error("Attempted to remove PriceLevel for '{}' from '{}' Monitor", priceLevel.getBackingTicker(),
					this.ticker);
		}

		switch (priceLevel.tradeIf()) {
		case FALLS_BELOW:
			this.fallsBelow.remove(priceLevel.getStrikePrice());
			break;
		case RISES_ABOVE:
			this.risesAbove.remove(priceLevel.getStrikePrice());
			break;
		default:
			logger.error("Unknown Price Direction: {}", priceLevel.tradeIf());
		}
	}
}
