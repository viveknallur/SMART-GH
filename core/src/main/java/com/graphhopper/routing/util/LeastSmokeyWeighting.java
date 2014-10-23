/*
 * Copyright 2014 elgammaa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;
import java.io.FileReader;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import org.ini4j.Ini;
//import java.io.PrintWriter;

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on the
 * noise data linked to edges stored in Redis
 * <p>
 * @author Amal Elgammal
 */
public class LeastSmokeyWeighting implements Weighting
{
    String currentCity;
    String sensorReading = "air";
    Jedis jedis;
    final double DEFAULT_SMOKE_VALUE = 2;

    public LeastSmokeyWeighting()
    {

    }

    public LeastSmokeyWeighting( String city ) throws JedisConnectionException, JedisDataException
    {
        this.currentCity = city;
        String host="";
        String fileName = "./sensors-config-files/" + this.currentCity + ".config";
        System.out.println("fileName = " + fileName);
        try
        {
            Ini ini = new Ini(new FileReader(fileName));
            host = ini.get("ConnectionSettings").fetch("REDIS_URL");
            System.out.println("jedisHost = " + host);
        } catch (IOException e)
        {
            System.out.println("IOError: " + e.getMessage());
        }

        try
        {
            jedis = new Jedis(host);

        } catch (JedisConnectionException e)
        {
            System.out.println("JedisConnectionException: " + e.getMessage());
        } catch (JedisDataException e)
        {
            System.out.println("JedisDataException: " + e.getMessage());

        } catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());

        }

    }

    @Override
    public double getMinWeight( double smokeValue )
    {
        //TODO: Check if this needs to be updated with other routing algorithms
        return smokeValue;
    }

    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse )
    {
        double smokeValue = getSmokeFromRedis(edge);
        return smokeValue;
    }

    
    double getSmokeFromRedis( EdgeIteratorState edge )
    {
        double smokeValue = DEFAULT_SMOKE_VALUE;
        String ntime;

        String edgeName = edge.getName();

        if (edgeName.length() > 0)
        {
            if (edgeName.contains(","))
            {
                edgeName = edgeName.split(",")[0];
            }
            
            edgeName = this.currentCity + "_" + this.sensorReading + "_" + edgeName;
            System.out.println("edgeName = " + edgeName);
            if (jedis.exists(edgeName))
            {
                smokeValue = Double.parseDouble(jedis.hget(edgeName, "value"));
                System.out.println("smokeValue = " + smokeValue);
                ntime = jedis.hget(edgeName, "timestamp");
            }
        }

        //TODO: check what to do with time and how to amend the instruction list with noise readings and timestamp 
        return smokeValue;
    }

    @Override
    public String toString()
    {
        return "LEAST_AIR_POLLUTION";
    }

}
