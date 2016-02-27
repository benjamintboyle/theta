package theta.execution.manager;

import theta.connection.manager.ConnectionManager;

public class ExecutionManagerTest {

	public static ExecutionManager buildExecutionManager(ConnectionManager connectionManager) {
		return new ExecutionManager(connectionManager);
	}
}
