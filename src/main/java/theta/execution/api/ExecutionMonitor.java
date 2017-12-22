package theta.execution.api;

import theta.domain.api.Security;

public interface ExecutionMonitor {
  public Boolean portfolioChange(Security security);
}
