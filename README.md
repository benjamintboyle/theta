# theta
Client to trade on Interactive Brokers specifically for Theta trades (straddle with cover).

The client interfaces with the Interactive Brokers (IB) Gateway via the IB API. The client autheticates with IB, downloads current positions, then maintains the "Theta" trade on those positions. The Theta positions are maintained by automatically trading the stock leg based on the Trade Requirements. The default Trade Requirement is to simply sell when an equity moves above and specific price, and buy when it falls below. There are other types of Trade Requirements. The client monitors the current price of the underlying equity automatically based on the current trades within the positions/portfolio.
