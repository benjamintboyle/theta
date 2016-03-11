package theta.domain.api;

public interface Security {

	public SecurityType getSecurityType();

	public String getTicker();

	public Integer getQuantity();

	@Override
	public String toString();

	public Double getPrice();
}
