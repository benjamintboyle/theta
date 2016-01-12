package theta.managers.api;

import theta.strategies.ExtrinsicCapture;

public interface Monitor {

	public void addMonitor(ExtrinsicCapture trade);
}
