/**
 *
 * SensorSubmission - RedisDataConnection.java
 * @date 31 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package general;

import redis.clients.jedis.Jedis;

/**
 * Interface between the application layer and the data layer.
 * DB connection using the jedis library
 */
public class RedisDataConnection {
	/**
	 * Port in which the redis intance is running
	 */
	private static final int PORT = 6379;
	
	private Jedis jedis;
	
	public RedisDataConnection() {
		jedis = new Jedis("localhost", PORT);
		System.out.println("Connection to server successfully on port: " + PORT);
	}
	
	public void insertInDB(String name, String meassure) {
		System.out.println("Information submitted to the server (" + Constants.CITY+name + ", " + meassure +")");
		jedis.set(Constants.CITY+name, meassure);
	}
}
