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
import java.util.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import org.ini4j.Ini;
//import java.io.PrintWriter;

/**
 * Calculates the least air polluted route- independent of a vehicle as the calculation is based on
 * the air quality data linked to edges stored in Redis
 * <p>
 * @author Amal Elgammal
 */
public class LeastSmokeyWeighting implements Weighting
{
    String currentCity;
    String sensorReading = "air";
    Jedis jedis;
    private double DEFAULT_SMOKE_VALUE;

    public LeastSmokeyWeighting()
    {

    }

    public LeastSmokeyWeighting( String city ) throws JedisConnectionException, JedisDataException
    {

        this.currentCity = city;

        String host = "";
        String realPath = getClass().getResource("/").getPath();
        //String fileName = "./sensors-config-files/" + this.currentCity + ".config";
        String fileName = realPath + this.currentCity + ".config";
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

        DEFAULT_SMOKE_VALUE = getAvgSmokeValue();
        System.out.println(" DEFAULT_SMOKE_VALUE = " + DEFAULT_SMOKE_VALUE);
    }

    double getAvgSmokeValue()
    {
        double medianSmokeValue = 3;
        String pattern = currentCity + "_" + sensorReading + "_*";
        System.out.println(" pattern = " + pattern);
        Set matchedRedisKeys = jedis.keys(pattern);

        if (matchedRedisKeys.size() > 0)
        {
            ArrayList<String> array = new ArrayList();
            array.addAll(matchedRedisKeys);
            Collections.sort(array);
            System.out.println(" array length after sorting = " + array.size());

            int middle = array.size() / 2;

            System.out.println(" middle = " + middle);
            if (array.size() % 2 == 1)
            {
                medianSmokeValue = Double.parseDouble(jedis.hget(array.get(middle), "value"));
                return medianSmokeValue;
            } else
            {
                medianSmokeValue = (Double.parseDouble(jedis.hget(array.get(middle - 1), "value"))
                        + Double.parseDouble(jedis.hget(array.get(middle - 1), "value"))) / 2;
                return medianSmokeValue;
            }

        } else
        {
            System.out.print("medianSmokeValue if no smoke data is available = " + medianSmokeValue);
            return medianSmokeValue;
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
