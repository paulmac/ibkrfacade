# IBKR Facade
## Interactive Brokers API wrapper and trading toolbox 
This project is built around the [Interactive Broker's TWS library](https://interactivebrokers.github.io/tws-api/) for Java. If you have a TWS or IB Gateway running, you can use this as a market data source for time series analysis, or even as a market gateway for trading bots.

**In this project you'll find:**

- The basic functions of the TWS API exposed to a Rest API
    - Searching in Interactive Brokers' instrument master data
    - Subscribe to market data for certain instruments
    - Create and manager orders
- Market data streamed into a Redis server from where you can do it whatever you want (eg.: using as a data source by creating a pub-sub message queue; or stream it to a time series database for further analitycs, build historical datasets, etc.)
- Built-in sample trading strategy (not a real working strategy, just an example of course), which is based on periodical time series analysis looking for trading signal and placing orders.


## Prerequisites

### TWS or IB gateway
In order to access market data you'll need an account at Interactive Brokers with subscription to those markets and instruments you need.

Furthermore, you have to install a [Trading Workstation](https://www.interactivebrokers.com/en/index.php?f=14099#tws-software) (TWS) or an [IB Gateway](https://www.interactivebrokers.com/en/?f=/en/trading/ibgateway-stable.php) which provide access to Interactive Brokers' infrastructure. If you are using TWS, make sure that the API connection is enabled, and if you want to placing orders through the library, the connection is not restricted to "read-only".

**See more:** [TWS API initial setup](https://interactivebrokers.github.io/tws-api/initial_setup.html)

### TWS API
You have to download the TWS API from https://interactivebrokers.github.io for Java. It contains a TwsApi.jar file which you need to have on your classpath. (You can find a working version of it in the /lib folder of this repository.) This is a legacy JAR maintained by Interactive Brokers and you cannot find it in the central Maven repository, so if you want to build this project with Maven, you need to manually add it to your local artifact repository (see pom.xml dependencies section), or make it available on the classpath some other way.

### Redis server
Redis is a powerful and extremely fast tool for storing market data. If you have a Redis server with RedisTimeSeries extension up and running, you can configure its access through the application.properties file, then the library will send the market data to the Redis for the subscribed instruments.


## How to use
1. Checkout the project
2. Add the `lib/TwsApi.jar` to the classpath from your IDE or alternatively publish it to a maven repository. Don't forget to modify `pom.xml` if needed!
3. Check your `application.properties` for:
   - TWS or IB gateway host and port number
   - URL settings for REST API
   - Redis server connection 
4. Build the project with maven
5. If you have your setup up and running, IBKR Facade should connect to Interactive Brokers automatically.

You will be able efficiently use the software only if you are familiar with the terminology and concepts used by Interactive Brokers, especially [Contracts](https://interactivebrokers.github.io/tws-api/contracts.html) and `conid` as the unique IB contract identifier.

**See:** [Search Interactive Brokers Contract Database](https://www.interactivebrokers.com/en/index.php?f=463) 

## Use cases

### REST API
Every basic function needed for using the system is exposed through a REST API, such as:
- Contract search
- Subscription to the market feed for certain contracts
- Getting price information
- Placing orders
- Getting positions

**See:** [Detailed information about the API endpoints](/swagger-docs)

### Market data analysis
If you have your Redis ready you can subscribe to market data feed of any instrument available on Interactive Brokers through your brokerage account.

From Java, you can use the `ContractManagerService.subscribe()` method or via HTTP you can use the `/subscribe` endpoint. If everything works as expected, the Contract itself should be saved to Redis under the conid of the Contract as key, and two time series should be created for storing the price information.

If you want to change or extend the functionality of this, you need to extend the `TimeSeriesHandler` class. You can utilize the full power of Redis from calculating OHLCV data automatically to using it as a pub/sub service, it's all up to you. 

**See:** [RedisTimeSeries](https://redis.io/docs/stack/timeseries)

### Trading strategy automation
Since you have hands-on market data, with the methods of `OrderManagerService`, `ContractManagerService` and `PositionManagerService` there is no predetermined way how to implement a trading strategy, the only limit is your imagination :)  

You can find an example implementation in the `strategy` package, which periodically checks the prices from Redis looking for a trading signal. Once the trade performed, it checks for an exit.