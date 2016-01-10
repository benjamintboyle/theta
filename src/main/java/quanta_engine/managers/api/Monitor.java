package quanta_engine.managers.api;

import quanta_engine.strategies.ExtrinsicCapture;

public interface Monitor {

	public void addMonitor(ExtrinsicCapture trade);
}
