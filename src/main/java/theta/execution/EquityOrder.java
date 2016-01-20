package theta.execution;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import theta.execution.api.Executable;
import theta.execution.api.ExecutionAction;
import theta.execution.api.ExecutionType;
import theta.managers.ExecutionManager;
import theta.strategies.api.Security;
import theta.strategies.api.SecurityType;

public class EquityOrder implements Executable {
	private final Logger logger = LoggerFactory.getLogger(ExecutionManager.class);

	private SecurityType securityType = SecurityType.STOCK;
	private Integer quantity;
	private ExecutionAction action;
	private ExecutionType executionType;

	public EquityOrder(Integer quantity, ExecutionAction action, ExecutionType executionType) {
		this.quantity = quantity;
		this.action = action;
		this.executionType = executionType;
	}

	public SecurityType getSecurityType() {
		return this.securityType;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public ExecutionAction getExecutionAction() {
		return this.action;
	}

	public ExecutionType getExecutionType() {
		return this.executionType;
	}

	@Override
	public Boolean validate(Security security) {
		HashSet<Boolean> isValid = new HashSet<Boolean>();

		isValid.add(this.isValidSecurityType(security.getSecurityType()));
		isValid.add(this.isAbsoluteQuantityEqualOrLess(security.getQuantity()));
		isValid.add(this.isValidAction(security.getQuantity()));
		return isValid.contains(Boolean.FALSE);
	}

	private Boolean isValidAction(Integer quantity) {
		Boolean isValidAction = Boolean.FALSE;

		switch (this.action) {
		case BUY:
			if (quantity < 0) {
				isValidAction = Boolean.FALSE;
			}
		case SELL:
			if (quantity > 0) {
				isValidAction = Boolean.TRUE;
			}
		default:
			isValidAction = Boolean.FALSE;
			this.logger.error("Invalid execution action: {}", this.action);
		}

		return isValidAction;
	}

	private Boolean isAbsoluteQuantityEqualOrLess(Integer quantity) {
		return Math.abs(quantity) <= Math.abs(this.quantity);
	}

	private Boolean isValidSecurityType(SecurityType securityType) {
		return this.securityType.equals(securityType);
	}
}