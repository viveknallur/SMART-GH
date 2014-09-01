/*
 * Copyright 2014 Amal Elgammal
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
import java.util.Random;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.*;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

/**
 * Calculates the least noisy route- independent of a vehicle as the calculation is based on the
 * noise data linked to edges stores in Redis
 * <p>
 * @author Amal Elgammal
 */
public class LeastNoisyWeighting implements Weighting
{
    String currentCity;

    public LeastNoisyWeighting()
    {
        System.out.println("LeastNoiseWeighting instantiated!");

    }

    @Override
    public double getMinWeight( double noiseValue )
    {
        //TODO: Check if this needs to be updated
        return noiseValue;
    }

    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse )
    {
        //Experimenting with returning a random radom between 0-80...Worked!
        Random nw = new Random();
        double returnedRandom = nw.nextInt(100);

        System.out.println("edge.getEdge() = " + edge.getEdge());
        System.out.println("edge.getFlags() = " + edge.getFlags());
        System.out.println("edge.getName() = " + edge.getName());

        return returnedRandom;

        //double noiseValue = getNoiseFromRedis(edge);
        //return noiseValue;
    }

    double getNoiseFromRedis( EdgeIteratorState edge ) throws JedisConnectionException, JedisDataException
    {
        double noiseValue = 0;
        String city = getCurrentCity();
        //TODO: connect to redis
        //The Python code to be extended to include the type of the readings with the key for each has
        //i.e.edge_type (type is the reading type from the relevant config file)
        //check if we can increment the time of the noise reading to instructions in the response??

        try
        {

            Jedis jedis = new Jedis("localhost");
            String hashname = "dublin_ways_set";
        //Map<String, String> decibles = jedis.hgetAll(hashname);
            //System.out.println("Noise Entries = " + decibles);
            Set<String> affectedEdges = jedis.smembers(hashname);
        //System.out.println("dublin_ways_set = " + affectedEdges);

            /*for(Map.Entry<String, String> value: decibles.entrySet())
             {
             String date = value.getKey();
             String noise = value.getValue();
             System.out.println("on " + date + ", noise was: " + noise);
             }*/
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

        return noiseValue;
    }

    //TODO: Start here: should be set in the route method (graphhopper) when selecting and init the appropriate weighting
    void setCurrentCity( String city )
    {
        this.currentCity = city;
    }

    String getCurrentCity()
    {
        return currentCity;

    }

    @Override
    public String toString()
    {
        //TODO: check if we need to register it with the encodering manger or elsewhere
        return "LEAST_NOISY";
    }

}
