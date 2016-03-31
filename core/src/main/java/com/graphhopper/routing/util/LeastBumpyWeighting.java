/*
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Calculates the least bumpy route- independent of a vehicle as the calculation is based on the
 * 'smoothness' data linked to edges stored in Redis
 * <p>
 */
public class LeastBumpyWeighting implements Weighting
{
    String currentCity;
    String sensorReading = "surface";
    private double DEFAULT_SURFACE_VALUE;
    Jedis jedis;

    public LeastBumpyWeighting()
    {

    }

    public LeastBumpyWeighting( String city ) throws JedisConnectionException, JedisDataException
    {
        this.currentCity = city;
        String host = "";
        String realPath = getClass().getResource("/").getPath();
        //String fileName = "./sensors-config-files/" + this.currentCity + ".config";
        String fileName = realPath+this.currentCity+".config";
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
        DEFAULT_SURFACE_VALUE = getAvgSurfaceValue();
        System.out.println(" DEFAULT_SURFACE_VALUE = " + DEFAULT_SURFACE_VALUE);

    }

    double getAvgSurfaceValue()
    {
        double medianSurfaceValue = 130;
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
                medianSurfaceValue = Double.parseDouble(jedis.hget(array.get(middle), "value"));
                return medianSurfaceValue;
            } else
            {
                medianSurfaceValue = (Double.parseDouble(jedis.hget(array.get(middle - 1), "value"))
                        + Double.parseDouble(jedis.hget(array.get(middle - 1), "value"))) / 2;
                return medianSurfaceValue;
            }

        } else
        {
            System.out.print("medianSurfaceValue if no surface data is available = " + medianSurfaceValue);
            return medianSurfaceValue;
        }
    }

    @Override
    public double getMinWeight( double surfaceValue )
    {
        //TODO: Check if this needs to be updated with other routing algorithms
        return surfaceValue;
    }

    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse )
    {
        double surfaceValue = getSurfaceFromRedis(edge);
        return surfaceValue;
    }

    double getSurfaceFromRedis( EdgeIteratorState edge )
    {
        double surfaceValue = 52;
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
                surfaceValue = Double.parseDouble(jedis.hget(edgeName, "value"));
                System.out.println("surfaceValue = " + surfaceValue);
                ntime = jedis.hget(edgeName, "timestamp");
            }
        }

        //TODO: check what to do with time and how to amend the instruction list with surface readings and timestamp 
        return surfaceValue;
    }

    @Override
    public String toString()
    {
        //TODO: check if we need to register it with the encodering manger or elsewhere
        return "LEAST_BUMPY";
    }

}
