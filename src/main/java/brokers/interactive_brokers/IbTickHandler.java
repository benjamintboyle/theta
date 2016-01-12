package brokers.interactive_brokers;

import com.ib.controller.NewTickType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.Types.MktDataType;

import theta.ThetaEngine;
import theta.managers.PriceMonitor;
import theta.strategies.ThetaTrade;

public class IbTickHandler implements ITopMktDataHandler {
	private final Logger logger = LoggerFactory.getLogger(IbTickHandler.class);

	ThetaEngine controller;
	PriceMonitor callback;

	String ticker;
	double bidPrice;
	double askPrice;
	double lastPrice;
	long lastTime;
	int bidSize;
	int askSize;
	double closePrice;
	int volume;
	boolean isSnapshot;

	public IbTickHandler(ThetaEngine controller, PriceMonitor callback, ThetaTrade trade) {
		this.controller = controller;
		this.callback = callback;
		this.ticker = trade.getBackingTicker();
	}

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
			if (this.lastPrice != price) {
				this.lastPrice = price;
				this.callback.notifyPriceChange(this.ticker);
			}
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

	public String getTicker() {
		return this.ticker;
	}

	public Double getBid() {
		return this.bidPrice;
	}

	public Double getAsk() {
		return this.askPrice;
	}

	public Double getLast() {
		return this.lastPrice;
	}

	public LocalDateTime getLastTime() {
		return LocalDateTime.ofEpochSecond(this.lastTime, 0, ZoneOffset.UTC);
	}

	public Integer getBidSize() {
		return this.getBidSize();
	}

	public Integer getAskSize() {
		return this.getAskSize();
	}

	public Double getClose() {
		return this.closePrice;
	}

	public Integer getVolume() {
		return this.volume;
	}

	public Boolean isSnapshot() {
		return this.isSnapshot;
	}

	public void subscribeMarketData(ThetaTrade trade) {
		NewContract contract = trade.getEquity().getContract();
		contract.exchange("SMART");
		contract.primaryExch("ISLAND");

		this.controller.controller().reqTopMktData(contract, "", false, this);
	}

	public void unsubscribeMarketData() {
		this.controller.controller().cancelTopMktData(this);
	}
}
